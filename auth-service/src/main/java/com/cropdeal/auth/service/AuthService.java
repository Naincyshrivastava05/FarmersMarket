package com.cropdeal.auth.service;

import com.cropdeal.auth.dto.AuthResponse;
import com.cropdeal.auth.dto.LoginRequest;
import com.cropdeal.auth.dto.RegisterRequest;
import com.cropdeal.auth.model.User;
import com.cropdeal.auth.repository.UserRepository;
import com.cropdeal.auth.security.JwtUtil;
import com.cropdeal.common.event.EventEnvelope;
import com.cropdeal.common.event.EventTypes;
import com.cropdeal.common.event.UserRegisteredEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Contains all business logic for auth-service. Controllers stay thin
 * (just HTTP plumbing) and this class does the real work.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RabbitTemplate rabbitTemplate;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.rabbitTemplate = rabbitTemplate;
    }

    public AuthResponse register(RegisterRequest request) {

        // Guard: reject duplicate emails before we even try to save.
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered: " + request.email());
        }

        // Hash the password -- NEVER store what the user typed directly.
        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.role()
        );

        userRepository.save(user);

        // Publish user.registered event so farmer-service or dealer-service
        // can create the matching profile row. We do this AFTER saving
        // successfully -- if the save fails, we never publish the event,
        // so downstream services don't create orphaned profiles.
        UserRegisteredEvent payload = new UserRegisteredEvent(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );

        rabbitTemplate.convertAndSend(
                EventTypes.EXCHANGE,
                EventTypes.USER_REGISTERED,
                new EventEnvelope<>(EventTypes.USER_REGISTERED, payload)
        );

        // Issue a JWT immediately so the user is logged in right after
        // registering, without a separate login call.
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(
                token,
                user.getRole().name(),
                user.getEmail(),
                user.getId()
        );
    }

    public AuthResponse login(LoginRequest request) {

        // AuthenticationManager checks the email + password against the
        // database using Spring Security's UserDetailsService (which we'll
        // add shortly). If credentials are wrong it throws
        // BadCredentialsException automatically -- we don't need to check
        // manually.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(
                token,
                user.getRole().name(),
                user.getEmail(),
                user.getId()
        );
    }
}
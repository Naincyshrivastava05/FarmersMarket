package com.cropdeal.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures Spring Security for auth-service.
 *
 * Key decisions made here:
 *
 * 1. STATELESS session -- we don't use HTTP sessions at all. Every
 *    request must carry a JWT. This is mandatory for microservices
 *    because there's no shared session store between services.
 *
 * 2. CSRF disabled -- CSRF attacks target session cookies. Since we're
 *    using JWT in Authorization headers (not cookies), CSRF is not a
 *    relevant attack vector here, and disabling it simplifies our API.
 *
 * 3. Only /api/auth/** is public -- everything else requires a valid
 *    token. Register and login are the only endpoints that don't need
 *    a token (obviously, since the whole point is to GET a token).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }

    /**
     * BCrypt is the standard password hashing algorithm -- it is
     * deliberately slow (to make brute force attacks expensive) and
     * includes a built-in salt (so identical passwords hash differently
     * each time, preventing rainbow table attacks).
     * NEVER store plain text passwords -- always hash through this bean.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager is what Spring Security uses internally to
     * verify username+password during login. We expose it as a bean so
     * our AuthService can call it directly.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
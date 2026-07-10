package com.cropdeal.auth.controller;

import com.cropdeal.auth.dto.AuthResponse;
import com.cropdeal.auth.dto.LoginRequest;
import com.cropdeal.auth.dto.RegisterRequest;
import com.cropdeal.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Thin HTTP layer -- receives the request, calls the service, returns
 * the response. No business logic lives here. The @Valid annotation
 * on each parameter triggers the validation rules we put on RegisterRequest
 * and LoginRequest (the @NotBlank, @Email annotations) -- if validation
 * fails, Spring returns a 400 automatically before this method even runs.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
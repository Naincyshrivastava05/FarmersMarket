package com.cropdeal.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.cropdeal.auth.model.Role;

/**
 * Request body for POST /api/auth/register.
 * Validation annotations here mean Spring automatically rejects any
 * request missing these fields before it even reaches our service
 * logic -- no manual null checks needed in the controller.
 */
public record RegisterRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        String password,

        @NotNull(message = "Role is required")
        Role role
) {
}
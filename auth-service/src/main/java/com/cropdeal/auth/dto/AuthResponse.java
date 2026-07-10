package com.cropdeal.auth.dto;
import java.util.UUID;
/**
 * Response body returned after successful register or login.
 * Returns the JWT token and the user's role so the frontend/client
 * knows immediately what it's allowed to do, without making another
 * request to figure that out.
 */
public record AuthResponse(
        String token,
        String role,
        String email,
        UUID userId
) {
}
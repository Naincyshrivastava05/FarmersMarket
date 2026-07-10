package com.cropdeal.gateway.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Decides which routes are public (no JWT required) vs protected.
 *
 * Public routes are only the auth endpoints -- register and login
 * are the only things you can do without a token, because they're
 * how you GET a token in the first place.
 *
 * Everything else (browsing crops, placing orders, viewing payments)
 * requires a valid JWT -- meaning the user must have logged in first.
 */
@Component
public class RouteValidator {

    // Exact path prefixes that bypass JWT validation.
    private static final List<String> PUBLIC_ROUTES = List.of(
            "/api/auth/register",
            "/api/auth/login"
    );

    public boolean isPublicRoute(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return PUBLIC_ROUTES.stream()
                .anyMatch(path::startsWith);
    }
}
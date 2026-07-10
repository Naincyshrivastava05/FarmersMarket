package com.cropdeal.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * A GatewayFilter that runs on every request to protected routes.
 * It checks the Authorization header for a valid Bearer token
 * before forwarding the request to the downstream service.
 *
 * Why a GatewayFilterFactory and not a regular servlet filter?
 * The gateway runs on Spring WebFlux (reactive/non-blocking),
 * not Spring MVC (servlet-based). WebFlux uses a different filter
 * chain -- GatewayFilterFactory is the correct hook point here.
 * This is the one place in the whole project where reactive
 * programming shows up, because everything flows through the
 * gateway under high concurrency.
 *
 * Flow:
 * Request arrives → this filter checks JWT → if valid, forward
 * to downstream service with email+role headers injected →
 * if invalid, return 401 immediately, never reach downstream.
 */
@Component
public class AuthenticationFilter extends
        AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    private final RouteValidator routeValidator;

    public AuthenticationFilter(JwtUtil jwtUtil,
                                RouteValidator routeValidator) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.routeValidator = routeValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            // Skip JWT check for public routes (register, login).
            if (routeValidator.isPublicRoute(exchange.getRequest())) {
                return chain.filter(exchange);
            }

            // All other routes require a valid Bearer token.
            HttpHeaders headers = exchange.getRequest().getHeaders();

            if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("Unauthorized request to {}: missing Authorization header",
                        exchange.getRequest().getURI().getPath());
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String authHeader = headers
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null
                    || !authHeader.startsWith("Bearer ")) {
                log.warn("Unauthorized request to {}: invalid Authorization header",
                        exchange.getRequest().getURI().getPath());
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.isTokenValid(token)) {
                log.warn("Unauthorized request to {}: invalid token",
                        exchange.getRequest().getURI().getPath());
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            // Token is valid -- extract email and role, inject them
            // as headers so downstream services know who made the
            // request without re-validating the token themselves.
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("X-Auth-Email", email)
                            .header("X-Auth-Role", role)
                            .build())
                    .build();

            return chain.filter(mutatedExchange);
        };
    }

    /**
     * Returns an error response and completes the exchange --
     * the request never reaches the downstream service.
     */
    private Mono<Void> onError(ServerWebExchange exchange,
                                HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Config class is required by AbstractGatewayFilterFactory
        // even if empty -- it's the type parameter for the factory.
    }
}
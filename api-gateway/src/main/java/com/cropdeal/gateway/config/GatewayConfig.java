package com.cropdeal.gateway.config;

import com.cropdeal.gateway.security.AuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Programmatic route configuration -- wires the AuthenticationFilter
 * onto every route so every request passes through JWT validation.
 *
 * We define routes here in Java code rather than only in application.yml
 * because we need to attach the AuthenticationFilter to each route.
 * Filters can't be attached to yml-defined routes as easily as here.
 *
 * The routes defined here OVERRIDE the ones in application.yml --
 * so the yml routes are now just documentation of what ports we use.
 */
@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authenticationFilter;

    public GatewayConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // Auth routes -- filter still runs but RouteValidator
                // marks /api/auth/** as public so JWT check is skipped.
        		.route("chatbot-service", r -> r
        		        .path("/api/chatbot/**")
        		        .filters(f -> f.filter(
        		                authenticationFilter.apply(
        		                        new AuthenticationFilter.Config())))
        		        .uri("http://localhost:8087"))
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f.filter(
                                authenticationFilter.apply(
                                        new AuthenticationFilter.Config())))
                        .uri("http://localhost:8081"))

                .route("farmer-service", r -> r
                        .path("/api/farmers/**")
                        .filters(f -> f.filter(
                                authenticationFilter.apply(
                                        new AuthenticationFilter.Config())))
                        .uri("http://localhost:8082"))

                .route("dealer-service", r -> r
                        .path("/api/dealers/**")
                        .filters(f -> f.filter(
                                authenticationFilter.apply(
                                        new AuthenticationFilter.Config())))
                        .uri("http://localhost:8083"))

                .route("crop-service", r -> r
                        .path("/api/crops/**")
                        .filters(f -> f.filter(
                                authenticationFilter.apply(
                                        new AuthenticationFilter.Config())))
                        .uri("http://localhost:8084"))

                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f.filter(
                                authenticationFilter.apply(
                                        new AuthenticationFilter.Config())))
                        .uri("http://localhost:8085"))

                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f.filter(
                                authenticationFilter.apply(
                                        new AuthenticationFilter.Config())))
                        .uri("http://localhost:8086"))

                .build();
    }
}
package com.cropdeal.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configures CORS (Cross-Origin Resource Sharing) for the gateway.
 *
 * Why reactive (CorsWebFilter) instead of the usual WebMvcConfigurer?
 * The gateway runs on Spring WebFlux, not Spring MVC -- WebFlux has
 * its own CORS filter class. Using the MVC version here would silently
 * do nothing, which is a common mistake.
 *
 * This is the ONE place in the whole system where CORS is configured --
 * because all requests go through the gateway first. We never need to
 * add CORS config to individual services (auth, crop, order etc.)
 * since they never talk directly to the browser.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {

        CorsConfiguration config = new CorsConfiguration();

        // Which frontend origins are allowed to call the gateway.
        // In production replace with your real frontend domain.
        config.setAllowedOrigins(List.of(
                "http://localhost:3000"
        ));

        // Which HTTP methods are allowed.
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE",
                "PATCH", "OPTIONS"
        ));

        // Which headers the frontend is allowed to send.
        // Authorization is critical -- this is how the JWT travels.
        config.setAllowedHeaders(List.of("*"));

        // Allow the browser to send cookies and Authorization
        // headers with cross-origin requests.
        config.setAllowCredentials(true);

        // How long the browser can cache the preflight response
        // in seconds -- 1 hour means the browser won't send a
        // preflight before EVERY request, just the first one.
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        // Apply this CORS config to every route on the gateway.
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
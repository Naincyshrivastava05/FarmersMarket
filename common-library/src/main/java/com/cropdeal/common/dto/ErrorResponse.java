package com.cropdeal.common.dto;

import java.time.Instant;

/**
 * Standard error body every service should return for 4xx/5xx responses.
 * Sharing this one shape across all six services means a frontend (or
 * another service reading an error response) only has to know how to parse
 * errors ONCE, rather than handling a different shape per service.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path);
    }
}
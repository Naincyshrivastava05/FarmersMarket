package com.cropdeal.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload for "user.registered" (design doc 3.1).
 * Published by auth-service, consumed by farmer-service and dealer-service
 * to create the corresponding profile shell row keyed by userId.
 */
public record UserRegisteredEvent(
        UUID userId,
        String email,
        String role,
        Instant registeredAt
) {
}
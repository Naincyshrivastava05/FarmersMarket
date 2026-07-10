package com.cropdeal.common.dto;

import java.util.UUID;

/**
 * Response shape for the synchronous Feign call order-service makes to
 * dealer-service: "does this dealer exist and are they active?"
 */
public record DealerStatusResponse(
        UUID dealerId,
        boolean exists,
        boolean active
) {
}
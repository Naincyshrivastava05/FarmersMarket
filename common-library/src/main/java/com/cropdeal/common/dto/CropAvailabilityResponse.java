package com.cropdeal.common.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response shape for the synchronous Feign call order-service makes to
 * crop-service: "is this crop still available at this quantity?"
 * (design doc section 2 - this is a SYNC call because order-service cannot
 * decide whether to accept the order without this answer first.)
 *
 * This lives in common-library, not in crop-service, because BOTH sides of
 * a Feign call need to agree on the shape: crop-service serializes it,
 * order-service's Feign client deserializes into the exact same class.
 * Sharing the class guarantees they can never drift apart silently -
 * if crop-service changes the shape, order-service's code fails to compile
 * instead of failing at runtime with a deserialization error.
 */
public record CropAvailabilityResponse(
        UUID cropId,
        boolean available,
        int availableQuantity,
        BigDecimal unitPrice
) {
}
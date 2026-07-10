package com.cropdeal.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload for "order.cancelled" (design doc 3.3).
 * Published by order-service, consumed by crop-service to release inventory
 * that had been reserved for this order back into available stock.
 */
public record OrderCancelledEvent(
        UUID orderId,
        String reason,
        Instant cancelledAt
) {
}
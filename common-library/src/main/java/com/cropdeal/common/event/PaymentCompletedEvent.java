package com.cropdeal.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Payload for "payment.completed" (design doc 3.4).
 * Published by payment-service, consumed by order-service, which reacts by
 * transitioning the order from PENDING_PAYMENT to CONFIRMED.
 */
public record PaymentCompletedEvent(
        UUID orderId,
        UUID paymentId,
        BigDecimal amount,
        String method,
        Instant completedAt
) {
}
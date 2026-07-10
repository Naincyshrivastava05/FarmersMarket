package com.cropdeal.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload for "payment.failed" (design doc 3.5).
 * Published by payment-service, consumed by order-service, which transitions
 * the order to PAYMENT_FAILED and is then responsible for publishing
 * order.cancelled itself so crop-service releases reserved inventory.
 * (Payment-service does not publish order.cancelled directly - that would
 * blur ownership; order-service owns order state transitions.)
 */
public record PaymentFailedEvent(
        UUID orderId,
        UUID paymentId,
        String reason,
        Instant failedAt
) {
}
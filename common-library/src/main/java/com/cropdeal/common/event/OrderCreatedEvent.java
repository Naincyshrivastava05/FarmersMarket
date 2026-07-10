package com.cropdeal.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Payload for the "order.created" event (design doc section 3.2).
 * Published by order-service, consumed by payment-service and crop-service.
 *
 * This is a Java "record" rather than a regular class. Records are a good
 * fit for event payloads specifically because:
 *   - They are immutable by construction (all fields final, no setters) --
 *     which matches reality: a payload describing something that already
 *     happened should never be mutated after the fact.
 *   - They auto-generate the constructor, getters (named orderId() not
 *     getOrderId()), equals/hashCode/toString -- cutting ~80 lines of
 *     boilerplate you'd otherwise hand-write for a simple data holder.
 *
 * Money is represented as BigDecimal, never double/float. Floating point
 * binary representation cannot exactly represent most decimal fractions
 * (0.1 + 0.2 != 0.3 in double arithmetic), which is unacceptable once real
 * currency amounts are involved -- BigDecimal does exact decimal math.
 */
public record OrderCreatedEvent(
        UUID orderId,
        UUID dealerId,
        UUID farmerId,
        List<OrderItem> items,
        BigDecimal totalAmount,
        Instant createdAt
) {
    /**
     * Nested record for a single line item within the order. Declared as a
     * record inside OrderCreatedEvent (rather than its own top-level file)
     * because it has no meaning or reuse outside this event's context.
     */
    public record OrderItem(
            UUID cropId,
            int quantity,
            BigDecimal unitPrice
    ) {
    }
}
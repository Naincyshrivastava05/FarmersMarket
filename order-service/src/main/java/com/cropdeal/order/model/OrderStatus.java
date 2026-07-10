package com.cropdeal.order.model;

/**
 * Every state an order can be in during its lifecycle.
 * The transitions are:
 *
 * PENDING_PAYMENT → CONFIRMED (payment succeeded)
 * PENDING_PAYMENT → PAYMENT_FAILED (payment failed)
 * PAYMENT_FAILED  → CANCELLED (order-service cancels after payment fails)
 * CONFIRMED       → CANCELLED (manual cancellation after confirmation)
 *
 * Having explicit states means you can always answer "where is this
 * order right now" by reading one field, without piecing together
 * multiple events or timestamps.
 */
public enum OrderStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    PAYMENT_FAILED,
    CANCELLED
}
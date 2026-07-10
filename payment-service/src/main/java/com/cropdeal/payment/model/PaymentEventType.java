package com.cropdeal.payment.model;

/**
 * The four things that can happen to a payment -- these become
 * rows in the payment_events append-only log, never overwritten.
 * Adding a new payment event type in future (e.g. REFUND_REQUESTED)
 * means adding a value here and a new handler -- existing rows
 * are never touched.
 */
public enum PaymentEventType {
    INITIATED,
    COMPLETED,
    FAILED,
    REFUNDED
}
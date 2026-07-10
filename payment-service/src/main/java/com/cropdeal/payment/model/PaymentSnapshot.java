package com.cropdeal.payment.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Performance optimization for event sourcing -- instead of
 * replaying ALL events for an orderId every time someone asks
 * "what's the current payment status", we periodically save
 * a snapshot of the current derived state.
 *
 * Next read starts from the snapshot's lastSequenceNo and only
 * replays events that came after it, instead of from the beginning.
 * For most orders (small number of events) this barely matters,
 * but it becomes important for orders with many payment retries.
 */
@Entity
@Table(name = "payment_snapshots")
public class PaymentSnapshot {

    @Id
    private UUID orderId;

    @Column(nullable = false)
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentEventType currentState;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    // The sequence number of the last event included in this snapshot.
    @Column(nullable = false)
    private Integer lastSequenceNo;

    @Column(nullable = false)
    private Instant updatedAt;

    protected PaymentSnapshot() {
    }

    public PaymentSnapshot(UUID orderId, UUID paymentId,
                           PaymentEventType currentState,
                           BigDecimal amount, Integer lastSequenceNo) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.currentState = currentState;
        this.amount = amount;
        this.lastSequenceNo = lastSequenceNo;
        this.updatedAt = Instant.now();
    }

    public void update(PaymentEventType newState,
                       Integer newSequenceNo) {
        this.currentState = newState;
        this.lastSequenceNo = newSequenceNo;
        this.updatedAt = Instant.now();
    }

    public UUID getOrderId() { return orderId; }
    public UUID getPaymentId() { return paymentId; }
    public PaymentEventType getCurrentState() { return currentState; }
    public BigDecimal getAmount() { return amount; }
    public Integer getLastSequenceNo() { return lastSequenceNo; }
    public Instant getUpdatedAt() { return updatedAt; }
}
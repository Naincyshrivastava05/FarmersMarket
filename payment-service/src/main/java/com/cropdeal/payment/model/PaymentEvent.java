package com.cropdeal.payment.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The append-only event log table from design doc section 4.
 * Every row is immutable once written -- no UPDATE statements
 * ever touch this table, only INSERTs.
 *
 * Current payment state is derived by reading all rows for an
 * orderId in sequence_no order and replaying them -- the last
 * event's type tells you where the payment stands right now.
 *
 * Why this matters: if someone asks "what happened to payment
 * for order X and when", you can answer completely and exactly
 * from this table. A single mutable "status" column can only
 * tell you where it ended up, not how it got there.
 */
@Entity
@Table(name = "payment_events")
public class PaymentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentEventType eventType;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    private String notes;

    // Monotonically increasing per orderId -- used to replay
    // events in the correct order and detect gaps.
    @Column(nullable = false)
    private Integer sequenceNo;

    @Column(nullable = false)
    private Instant occurredAt;

    protected PaymentEvent() {
    }

    public PaymentEvent(UUID orderId, UUID paymentId,
                        PaymentEventType eventType,
                        BigDecimal amount, String notes,
                        Integer sequenceNo) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.eventType = eventType;
        this.amount = amount;
        this.notes = notes;
        this.sequenceNo = sequenceNo;
        this.occurredAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getPaymentId() { return paymentId; }
    public PaymentEventType getEventType() { return eventType; }
    public BigDecimal getAmount() { return amount; }
    public String getNotes() { return notes; }
    public Integer getSequenceNo() { return sequenceNo; }
    public Instant getOccurredAt() { return occurredAt; }
}
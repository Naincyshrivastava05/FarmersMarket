package com.cropdeal.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.UUID;

/**
 * Every event published to RabbitMQ is wrapped in this envelope. This is a
 * deliberate decision from the design doc: instead of publishing raw payload
 * objects, we always publish EventEnvelope<SomePayload>.
 *
 * Why bother with a wrapper instead of just publishing OrderCreatedEvent
 * directly?
 *
 *  1. eventId lets every consumer deduplicate. RabbitMQ's delivery guarantee
 *     is "at least once", not "exactly once" -- a consumer can receive the
 *     same message twice (e.g. if it crashes after processing but before
 *     acknowledging). Consumers store seen eventIds and skip duplicates.
 *
 *  2. eventType lets a single queue carry multiple kinds of events if needed,
 *     and lets generic infrastructure code (logging, dead-letter handling)
 *     work without knowing the specific payload shape.
 *
 *  3. version future-proofs the contract. If OrderCreated payload needs a
 *     breaking change later, you bump version and consumers can branch on it
 *     instead of guessing from missing fields.
 *
 * The payload itself is generic (T) -- each concrete event payload class
 * (OrderCreatedEvent, PaymentCompletedEvent, etc.) is plugged in as T.
 */
public class EventEnvelope<T> {

    private String eventId;
    private String eventType;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant occurredAt;

    private int version;
    private T payload;

    // No-args constructor is required by Jackson for deserialization --
    // Jackson builds the object first with this, then sets fields one by one.
    public EventEnvelope() {
    }

    public EventEnvelope(String eventType, T payload) {
        // We generate the eventId and timestamp HERE, at construction time,
        // rather than asking the caller to supply them. This guarantees
        // every envelope is well-formed and removes a whole category of bug
        // where a service forgets to set eventId.
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.occurredAt = Instant.now();
        this.version = 1;
        this.payload = payload;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}
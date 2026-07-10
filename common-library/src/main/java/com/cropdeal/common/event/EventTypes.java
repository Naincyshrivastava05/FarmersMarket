package com.cropdeal.common.event;

/**
 * Centralizes the RabbitMQ exchange name and routing keys from section 3 of
 * the design doc. Without this class, every service would hardcode the
 * string "order.created" in its own code, and a typo in one service (e.g.
 * "order.craeted") would silently create a dead queue that nothing ever
 * receives. Because both the publisher and every consumer import the SAME
 * constant from common-library, that class of bug becomes a compile error
 * instead of a silent runtime failure.
 */
public final class EventTypes {

    // Private constructor -- this class is just a bag of constants, it
    // should never be instantiated.
    private EventTypes() {
    }

    /** The single topic exchange every CropDeal event is published to. */
    public static final String EXCHANGE = "cropdeal.events";

    // Routing keys, one per event from the design doc's section 3.
    public static final String USER_REGISTERED = "user.registered";
    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_CANCELLED = "order.cancelled";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String INVENTORY_UPDATED = "inventory.updated";
}
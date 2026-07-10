package com.cropdeal.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload for "inventory.updated" (design doc 3.6).
 * Published by crop-service whenever available quantity changes. No
 * consumer yet - reserved for a future search/cache service - but we
 * define the contract now so crop-service can start publishing it without
 * a breaking change later.
 */
public record InventoryUpdatedEvent(
        UUID cropId,
        int availableQuantity,
        Instant updatedAt
) {
}
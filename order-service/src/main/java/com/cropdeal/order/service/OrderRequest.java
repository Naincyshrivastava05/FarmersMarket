package com.cropdeal.order.service;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Request shape for creating an order.
 * Separate from the Order entity -- the controller receives this,
 * the service converts it into a real Order with locked prices.
 */
public record OrderRequest(

        @NotNull UUID dealerId,
        @NotNull UUID farmerId,
        @NotNull List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotNull UUID cropId,
            @Min(1) int quantity
    ) {
    }
}
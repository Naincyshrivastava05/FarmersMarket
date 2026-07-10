package com.cropdeal.chatbot.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Matches the Order entity JSON shape from order-service.
 */
public record OrderData(
        String id,
        String dealerId,
        String farmerId,
        List<OrderItemData> items,
        BigDecimal totalAmount,
        String status,
        String createdAt
) {
    public record OrderItemData(
            String cropId,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }
}
package com.cropdeal.chatbot.dto;

import java.math.BigDecimal;

/**
 * Matches the Crop entity JSON shape from crop-service.
 * Only fields the chatbot actually uses are included --
 * Jackson ignores unknown fields by default so extra fields
 * from the response are safely discarded.
 */
public record CropData(
        String id,
        String name,
        String description,
        String category,
        BigDecimal unitPrice,
        String unit,
        Integer availableQuantity,
        String status,
        String farmerId
) {
}
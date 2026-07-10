package com.cropdeal.crop.dto;

import com.cropdeal.crop.model.CropCategory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CropRequest(
        @NotNull UUID farmerId,
        @NotBlank String name,
        String description,
        @NotNull CropCategory category,
        @NotNull BigDecimal unitPrice,
        @NotBlank String unit,
        @Min(value = 1, message = "Quantity must be at least 1") Integer quantity
) {
}

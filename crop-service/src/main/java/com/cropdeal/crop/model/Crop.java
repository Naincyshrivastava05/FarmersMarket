package com.cropdeal.crop.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The core entity crop-service owns. Notice it tracks both
 * totalQuantity and availableQuantity separately -- totalQuantity
 * is what the farmer harvested, availableQuantity decrements as
 * orders come in. This lets us answer "how much is left to sell"
 * without destroying the original harvest record.
 */
@Entity
@Table(name = "crops")
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // The farmer who listed this crop -- logical reference to
    // farmer-service's userId, not a DB foreign key.
    @Column(nullable = false)
    private UUID farmerId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CropCategory category;

    // Price per unit (kg, quintal, etc.)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    private String unit;

    @Column(nullable = false)
    private Integer totalQuantity;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CropStatus status;

    @Column(nullable = false)
    private Instant listedAt;

    private Instant updatedAt;

    public enum CropStatus {
        AVAILABLE,
        PARTIALLY_AVAILABLE,
        SOLD_OUT
    }

    protected Crop() {
    }

    public Crop(UUID farmerId, String name, String description,
                CropCategory category, BigDecimal unitPrice,
                String unit, Integer totalQuantity) {
        this.farmerId = farmerId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = totalQuantity;
        this.status = CropStatus.AVAILABLE;
        this.listedAt = Instant.now();
    }

    /**
     * Decrements available quantity when an order is placed.
     * Updates status automatically based on remaining stock.
     * Returns false if not enough stock available.
     */
    public boolean reserveQuantity(int quantity) {
        if (this.availableQuantity < quantity) {
            return false;
        }
        this.availableQuantity -= quantity;
        this.updatedAt = Instant.now();

        if (this.availableQuantity == 0) {
            this.status = CropStatus.SOLD_OUT;
        } else if (this.availableQuantity < this.totalQuantity) {
            this.status = CropStatus.PARTIALLY_AVAILABLE;
        }
        return true;
    }

    /**
     * Releases quantity back when an order is cancelled.
     */
    public void releaseQuantity(int quantity) {
        this.availableQuantity += quantity;
        this.updatedAt = Instant.now();

        if (this.availableQuantity > 0) {
            this.status = CropStatus.AVAILABLE;
        }
    }

    public UUID getId() { return id; }
    public UUID getFarmerId() { return farmerId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public CropCategory getCategory() { return category; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getUnit() { return unit; }
    public Integer getTotalQuantity() { return totalQuantity; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public CropStatus getStatus() { return status; }
    public Instant getListedAt() { return listedAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public void setDescription(String description) { this.description = description; }
}
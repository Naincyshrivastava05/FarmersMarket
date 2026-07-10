package com.cropdeal.order.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a single crop line item within an order.
 * Stores the price at time of order -- unitPrice on the crop listing
 * can change later, but what the dealer agreed to pay is locked here
 * at order creation time.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // @JsonIgnore tells Jackson "don't serialize this field"
    // -- breaks the infinite loop:
    // Order → items → OrderItem → order → items → OrderItem...
    // Now it stops at: Order → items → OrderItem (no back reference)
    @JsonIgnore
    // ManyToOne links this item back to its parent order.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private UUID cropId;	

    @Column(nullable = false)
    private Integer quantity;

    // Price locked at order time, not a live reference to crop price.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    protected OrderItem() {
    }

    public OrderItem(UUID cropId, Integer quantity, BigDecimal unitPrice) {
        this.cropId = cropId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        // Subtotal computed once at creation, not recalculated later.
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public UUID getId() { return id; }
    public Order getOrder() { return order; }
    public UUID getCropId() { return cropId; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }

    public void setOrder(Order order) { this.order = order; }
}
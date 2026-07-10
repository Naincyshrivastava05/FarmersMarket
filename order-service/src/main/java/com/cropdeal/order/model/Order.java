package com.cropdeal.order.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID dealerId;

    @Column(nullable = false)
    private UUID farmerId;

    // OneToMany with CascadeType.ALL means when we save an Order,
    // all its OrderItems are saved automatically in the same
    // transaction -- we never save items separately.
    @OneToMany(mappedBy = "order",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    protected Order() {
    }

    public Order(UUID dealerId, UUID farmerId, BigDecimal totalAmount) {
        this.dealerId = dealerId;
        this.farmerId = farmerId;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.createdAt = Instant.now();
    }

    public void transitionTo(OrderStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public UUID getId() { return id; }
    public UUID getDealerId() { return dealerId; }
    public UUID getFarmerId() { return farmerId; }
    public List<OrderItem> getItems() { return items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
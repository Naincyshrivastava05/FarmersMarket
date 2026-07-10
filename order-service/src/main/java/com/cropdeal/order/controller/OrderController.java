package com.cropdeal.order.controller;

import com.cropdeal.order.model.Order;
import com.cropdeal.order.service.OrderRequest;
import com.cropdeal.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @Valid @RequestBody OrderRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(
                        request.dealerId(),
                        request.farmerId(),
                        request.items()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getById(orderId));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/dealer/{dealerId}")
    public ResponseEntity<List<Order>> getByDealer(
            @PathVariable UUID dealerId) {
        return ResponseEntity.ok(orderService.getByDealer(dealerId));
    }

    @GetMapping("/farmer")
    public ResponseEntity<List<Order>> getByFarmerQuery(
            @RequestParam UUID farmerId) {
        return ResponseEntity.ok(orderService.getByFarmer(farmerId));
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<Order>> getByFarmer(
            @PathVariable UUID farmerId) {
        return ResponseEntity.ok(orderService.getByFarmer(farmerId));
    }
}
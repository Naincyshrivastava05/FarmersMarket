package com.cropdeal.order.service;

import com.cropdeal.common.dto.CropAvailabilityResponse;
import com.cropdeal.common.dto.DealerStatusResponse;
import com.cropdeal.common.event.EventEnvelope;
import com.cropdeal.common.event.EventTypes;
import com.cropdeal.common.event.OrderCreatedEvent;
import com.cropdeal.common.event.OrderCancelledEvent;
import com.cropdeal.common.exception.ResourceNotFoundException;
import com.cropdeal.order.client.CropServiceClient;
import com.cropdeal.order.client.DealerServiceClient;
import com.cropdeal.order.model.Order;
import com.cropdeal.order.model.OrderItem;
import com.cropdeal.order.model.OrderStatus;
import com.cropdeal.order.repository.OrderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CropServiceClient cropServiceClient;
    private final DealerServiceClient dealerServiceClient;
    private final RabbitTemplate rabbitTemplate;

    public OrderService(OrderRepository orderRepository,
                        CropServiceClient cropServiceClient,
                        DealerServiceClient dealerServiceClient,
                        RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.cropServiceClient = cropServiceClient;
        this.dealerServiceClient = dealerServiceClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public Order createOrder(UUID dealerId, UUID farmerId,
                             List<OrderRequest.OrderItemRequest> items) {

        // STEP 1: Sync call -- verify dealer exists and is active.
        // We cannot accept an order from a suspended or non-existent dealer.
        DealerStatusResponse dealerStatus =
                dealerServiceClient.getDealerStatus(dealerId);

        if (!dealerStatus.exists() || !dealerStatus.active()) {
            throw new IllegalArgumentException(
                    "Dealer is not active: " + dealerId);
        }

        // STEP 2: Sync call -- verify each crop is available in
        // requested quantity before we commit to anything.
        for (OrderRequest.OrderItemRequest item : items) {
            CropAvailabilityResponse availability =
                    cropServiceClient.checkAvailability(
                            item.cropId(), item.quantity());

            if (!availability.available()) {
                throw new IllegalStateException(
                        "Crop not available in requested quantity: "
                                + item.cropId());
            }
        }

        // STEP 3: Build the order with locked prices from availability check.
        BigDecimal total = BigDecimal.ZERO;
        Order order = new Order(dealerId, farmerId, BigDecimal.ZERO);

        for (OrderRequest.OrderItemRequest item : items) {
            CropAvailabilityResponse availability =
                    cropServiceClient.checkAvailability(
                            item.cropId(), item.quantity());

            OrderItem orderItem = new OrderItem(
                    item.cropId(),
                    item.quantity(),
                    availability.unitPrice()
            );
            order.addItem(orderItem);
            total = total.add(orderItem.getSubtotal());
        }

        // Set the computed total before saving.
        order = new Order(dealerId, farmerId, total);
        for (OrderRequest.OrderItemRequest item : items) {
            CropAvailabilityResponse availability =
                    cropServiceClient.checkAvailability(
                            item.cropId(), item.quantity());
            order.addItem(new OrderItem(
                    item.cropId(), item.quantity(),
                    availability.unitPrice()));
        }

        orderRepository.save(order);

        // STEP 4: Async -- publish order.created so payment-service
        // charges the dealer and crop-service decrements inventory.
        // We publish AFTER saving -- if the save fails, no event
        // is published and downstream services stay consistent.
        List<OrderCreatedEvent.OrderItem> eventItems = order.getItems()
                .stream()
                .map(i -> new OrderCreatedEvent.OrderItem(
                        i.getCropId(),
                        i.getQuantity(),
                        i.getUnitPrice()))
                .toList();

        OrderCreatedEvent payload = new OrderCreatedEvent(
                order.getId(),
                order.getDealerId(),
                order.getFarmerId(),
                eventItems,
                order.getTotalAmount(),
                Instant.now()
        );

        rabbitTemplate.convertAndSend(
                EventTypes.EXCHANGE,
                EventTypes.ORDER_CREATED,
                new EventEnvelope<>(EventTypes.ORDER_CREATED, payload)
        );

        return order;
    }

    public Order getById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        ResourceNotFoundException.forId("Order", orderId));
    }

    public List<Order> getByDealer(UUID dealerId) {
        return orderRepository.findByDealerId(dealerId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getByFarmer(UUID farmerId) {
        return orderRepository.findByFarmerId(farmerId);
    }

    @Transactional
    public void confirmOrder(UUID orderId) {
        Order order = getById(orderId);
        order.transitionTo(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    @Transactional
    public void failOrder(UUID orderId) {
        Order order = getById(orderId);
        order.transitionTo(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);

        // After payment fails, cancel the order so crop-service
        // releases the reserved inventory -- design doc section 3.5.
        order.transitionTo(OrderStatus.CANCELLED);
        orderRepository.save(order);

        OrderCancelledEvent payload = new OrderCancelledEvent(
                order.getId(),
                "Payment failed",
                Instant.now()
        );

        rabbitTemplate.convertAndSend(
                EventTypes.EXCHANGE,
                EventTypes.ORDER_CANCELLED,
                new EventEnvelope<>(EventTypes.ORDER_CANCELLED, payload)
        );
    }
}
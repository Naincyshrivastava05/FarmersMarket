package com.cropdeal.crop.listener;

import com.cropdeal.common.event.EventEnvelope;
import com.cropdeal.common.event.OrderCancelledEvent;
import com.cropdeal.common.event.OrderCreatedEvent;
import com.cropdeal.crop.service.CropService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(OrderEventListener.class);

    private final CropService cropService;

    public OrderEventListener(CropService cropService) {
        this.cropService = cropService;
    }

    @RabbitListener(queues = "crop.order.created")
    public void handleOrderCreated(
            EventEnvelope<OrderCreatedEvent> envelope) {

        OrderCreatedEvent event = envelope.getPayload();
        log.info("Received order.created for orderId: {}",
                event.orderId());

        // An order can have multiple crop items -- decrement stock
        // for each one independently.
        event.items().forEach(item -> {
            try {
                cropService.reserveStock(item.cropId(), item.quantity());
                log.info("Reserved {} units of cropId: {}",
                        item.quantity(), item.cropId());
            } catch (Exception e) {
                log.error("Failed to reserve stock for cropId: {} - {}",
                        item.cropId(), e.getMessage());
            }
        });
    }

    @RabbitListener(queues = "crop.order.cancelled")
    public void handleOrderCancelled(
            EventEnvelope<OrderCancelledEvent> envelope) {

        OrderCancelledEvent event = envelope.getPayload();
        log.info("Received order.cancelled for orderId: {}",
                event.orderId());

        // Note: OrderCancelledEvent doesn't carry item details --
        // in a real system you'd look up the order items from a
        // read model or extend the event payload. For now we log
        // the cancellation -- this is one of the open questions
        // from the design doc to resolve later.
        log.info("Order cancelled: {} reason: {}",
                event.orderId(), event.reason());
    }
}
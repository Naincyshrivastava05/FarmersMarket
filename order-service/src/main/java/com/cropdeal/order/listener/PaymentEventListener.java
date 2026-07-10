package com.cropdeal.order.listener;

import com.cropdeal.common.event.EventEnvelope;
import com.cropdeal.common.event.PaymentCompletedEvent;
import com.cropdeal.common.event.PaymentFailedEvent;
import com.cropdeal.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentEventListener.class);

    private final OrderService orderService;

    public PaymentEventListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = "order.payment.completed")
    public void handlePaymentCompleted(
            EventEnvelope<PaymentCompletedEvent> envelope) {

        PaymentCompletedEvent event = envelope.getPayload();
        log.info("Payment completed for orderId: {}", event.orderId());
        orderService.confirmOrder(event.orderId());
    }

    @RabbitListener(queues = "order.payment.failed")
    public void handlePaymentFailed(
            EventEnvelope<PaymentFailedEvent> envelope) {

        PaymentFailedEvent event = envelope.getPayload();
        log.info("Payment failed for orderId: {} reason: {}",
                event.orderId(), event.reason());
        orderService.failOrder(event.orderId());
    }
}
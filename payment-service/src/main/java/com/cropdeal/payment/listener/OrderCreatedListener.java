package com.cropdeal.payment.listener;

import com.cropdeal.common.event.EventEnvelope;
import com.cropdeal.common.event.OrderCreatedEvent;
import com.cropdeal.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedListener {

    private static final Logger log =
            LoggerFactory.getLogger(OrderCreatedListener.class);

    private final PaymentService paymentService;

    public OrderCreatedListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @RabbitListener(queues = "payment.order.created")
    public void handleOrderCreated(
            EventEnvelope<OrderCreatedEvent> envelope) {

        OrderCreatedEvent event = envelope.getPayload();
        log.info("Received order.created for orderId: {} amount: {}",
                event.orderId(), event.totalAmount());


    }
}
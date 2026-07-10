package com.cropdeal.order.config;

import com.cropdeal.common.event.EventTypes;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // order-service consumes TWO events from payment-service:
    // payment.completed and payment.failed
    public static final String ORDER_PAYMENT_COMPLETED_QUEUE
            = "order.payment.completed";
    public static final String ORDER_PAYMENT_FAILED_QUEUE
            = "order.payment.failed";

    @Bean
    public TopicExchange cropDealExchange() {
        return new TopicExchange(EventTypes.EXCHANGE, true, false);
    }

    @Bean
    public Queue orderPaymentCompletedQueue() {
        return QueueBuilder.durable(ORDER_PAYMENT_COMPLETED_QUEUE).build();
    }

    @Bean
    public Queue orderPaymentFailedQueue() {
        return QueueBuilder.durable(ORDER_PAYMENT_FAILED_QUEUE).build();
    }

    @Bean
    public Binding orderPaymentCompletedBinding(
            Queue orderPaymentCompletedQueue,
            TopicExchange cropDealExchange) {
        return BindingBuilder
                .bind(orderPaymentCompletedQueue)
                .to(cropDealExchange)
                .with(EventTypes.PAYMENT_COMPLETED);
    }

    @Bean
    public Binding orderPaymentFailedBinding(
            Queue orderPaymentFailedQueue,
            TopicExchange cropDealExchange) {
        return BindingBuilder
                .bind(orderPaymentFailedQueue)
                .to(cropDealExchange)
                .with(EventTypes.PAYMENT_FAILED);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
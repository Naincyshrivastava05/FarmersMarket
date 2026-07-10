package com.cropdeal.payment.config;

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

    // payment-service consumes order.created and publishes
    // payment.completed or payment.failed in response.
    public static final String PAYMENT_ORDER_CREATED_QUEUE
            = "payment.order.created";

    @Bean
    public TopicExchange cropDealExchange() {
        return new TopicExchange(EventTypes.EXCHANGE, true, false);
    }

    @Bean
    public Queue paymentOrderCreatedQueue() {
        return QueueBuilder.durable(PAYMENT_ORDER_CREATED_QUEUE).build();
    }

    @Bean
    public Binding paymentOrderCreatedBinding(
            Queue paymentOrderCreatedQueue,
            TopicExchange cropDealExchange) {
        return BindingBuilder
                .bind(paymentOrderCreatedQueue)
                .to(cropDealExchange)
                .with(EventTypes.ORDER_CREATED);
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
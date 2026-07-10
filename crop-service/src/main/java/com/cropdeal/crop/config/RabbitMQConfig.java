package com.cropdeal.crop.config;

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

    // crop-service consumes TWO events:
    // 1. order.created  -- to decrement inventory
    // 2. order.cancelled -- to release inventory back
    public static final String CROP_ORDER_CREATED_QUEUE
            = "crop.order.created";
    public static final String CROP_ORDER_CANCELLED_QUEUE
            = "crop.order.cancelled";

    @Bean
    public TopicExchange cropDealExchange() {
        return new TopicExchange(EventTypes.EXCHANGE, true, false);
    }

    @Bean
    public Queue cropOrderCreatedQueue() {
        return QueueBuilder.durable(CROP_ORDER_CREATED_QUEUE).build();
    }

    @Bean
    public Queue cropOrderCancelledQueue() {
        return QueueBuilder.durable(CROP_ORDER_CANCELLED_QUEUE).build();
    }

    @Bean
    public Binding cropOrderCreatedBinding(
            Queue cropOrderCreatedQueue,
            TopicExchange cropDealExchange) {
        return BindingBuilder
                .bind(cropOrderCreatedQueue)
                .to(cropDealExchange)
                .with(EventTypes.ORDER_CREATED);
    }

    @Bean
    public Binding cropOrderCancelledBinding(
            Queue cropOrderCancelledQueue,
            TopicExchange cropDealExchange) {
        return BindingBuilder
                .bind(cropOrderCancelledQueue)
                .to(cropDealExchange)
                .with(EventTypes.ORDER_CANCELLED);
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
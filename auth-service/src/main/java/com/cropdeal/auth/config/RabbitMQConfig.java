package com.cropdeal.auth.config;

import com.cropdeal.common.event.EventTypes;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the RabbitMQ topology this service needs.
 *
 * Three concepts to understand here:
 *
 * EXCHANGE - think of it like a post office. Publishers send messages
 * TO the exchange, not directly to a queue. The exchange then decides
 * which queues to route the message to, based on the routing key.
 *
 * QUEUE - the actual mailbox where messages sit until a consumer reads
 * them. Each service that wants to receive a message gets its own queue.
 *
 * BINDING - the rule that connects a queue to an exchange, saying
 * "give this queue any message whose routing key matches this pattern".
 *
 * auth-service only PUBLISHES (it doesn't consume any events itself),
 * so we only need to declare the exchange here. The queues and bindings
 * are declared by the consuming services (farmer-service, dealer-service).
 * We still declare the exchange here because the publisher needs it to
 * exist before it can send anything.
 */
@Configuration
public class RabbitMQConfig {

    /**
     * Declares a Topic exchange -- "topic" means routing keys can use
     * wildcards (* matches one word, # matches zero or more words).
     * For example a consumer could bind with "user.*" to get all user
     * events, or "*.created" to get all created events across any domain.
     * durable(true) means the exchange survives a RabbitMQ server restart.
     */
    @Bean
    public TopicExchange cropDealExchange() {
        return new TopicExchange(EventTypes.EXCHANGE, true, false);
    }

    /**
     * Tells RabbitMQ to serialize message payloads as JSON using Jackson.
     * Without this, Spring AMQP uses Java's built-in serialization
     * (producing binary blobs that only Java can read, and that break
     * whenever you rename a class). JSON means any language can consume
     * the events, and messages are human-readable in the RabbitMQ UI.
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Wires the JSON converter into the RabbitTemplate so every
     * rabbitTemplate.convertAndSend(...) call in AuthService automatically
     * serializes to JSON without any extra steps.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
package com.cropdeal.farmer.config;

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

    /**
     * farmer-service gets its own dedicated queue for user.registered events.
     * dealer-service will have its OWN separate queue for the same event.
     * Both queues are bound to the same exchange with the same routing key,
     * so RabbitMQ delivers a copy of each user.registered message to BOTH
     * queues simultaneously -- this is the "fan-out via topic exchange" pattern.
     *
     * Why separate queues per service instead of one shared queue?
     * If they shared one queue, only ONE of them would receive each message
     * (queues compete for messages). With separate queues, both get their
     * own copy independently.
     */
    public static final String FARMER_USER_REGISTERED_QUEUE
            = "farmer.user.registered";

    @Bean
    public TopicExchange cropDealExchange() {
        return new TopicExchange(EventTypes.EXCHANGE, true, false);
    }

    @Bean
    public Queue farmerUserRegisteredQueue() {
        // durable(true) means the queue survives a RabbitMQ restart --
        // messages sitting in it won't be lost if RabbitMQ goes down.
        return QueueBuilder.durable(FARMER_USER_REGISTERED_QUEUE).build();
    }

    @Bean
    public Binding farmerUserRegisteredBinding(
            Queue farmerUserRegisteredQueue,
            TopicExchange cropDealExchange) {
        // Binds this queue to the exchange with routing key "user.registered"
        // -- meaning any message published with that routing key gets
        // delivered to this queue.
        return BindingBuilder
                .bind(farmerUserRegisteredQueue)
                .to(cropDealExchange)
                .with(EventTypes.USER_REGISTERED);
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
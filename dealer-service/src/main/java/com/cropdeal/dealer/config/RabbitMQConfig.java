package com.cropdeal.dealer.config;

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

    // Dealer-service gets its OWN dedicated queue -- separate from
    // farmer-service's queue. Both queues receive the same user.registered
    // events independently, so both services get their own copy.
    public static final String DEALER_USER_REGISTERED_QUEUE
            = "dealer.user.registered";

    @Bean
    public TopicExchange cropDealExchange() {
        return new TopicExchange(EventTypes.EXCHANGE, true, false);
    }

    @Bean
    public Queue dealerUserRegisteredQueue() {
        return QueueBuilder.durable(DEALER_USER_REGISTERED_QUEUE).build();
    }

    @Bean
    public Binding dealerUserRegisteredBinding(
            Queue dealerUserRegisteredQueue,
            TopicExchange cropDealExchange) {
        return BindingBuilder
                .bind(dealerUserRegisteredQueue)
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
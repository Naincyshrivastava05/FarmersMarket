package com.cropdeal.dealer.listener;

import com.cropdeal.common.event.EventEnvelope;
import com.cropdeal.common.event.UserRegisteredEvent;
import com.cropdeal.dealer.model.Dealer;
import com.cropdeal.dealer.repository.DealerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredListener {

    private static final Logger log =
            LoggerFactory.getLogger(UserRegisteredListener.class);

    private final DealerRepository dealerRepository;

    public UserRegisteredListener(DealerRepository dealerRepository) {
        this.dealerRepository = dealerRepository;
    }

    @RabbitListener(queues = "dealer.user.registered")
    public void handleUserRegistered(
            EventEnvelope<UserRegisteredEvent> envelope) {

        UserRegisteredEvent event = envelope.getPayload();

        // Only create a dealer profile for DEALER role --
        // farmer-service handles FARMER registrations.
        if (!"DEALER".equals(event.role())) {
            log.info("Skipping user.registered for role {}: {}",
                    event.role(), event.email());
            return;
        }

        if (dealerRepository.existsByUserId(event.userId())) {
            log.warn("Dealer profile already exists for userId: {}",
                    event.userId());
            return;
        }

        Dealer dealer = new Dealer(event.userId(), event.email());
        dealerRepository.save(dealer);

        log.info("Created dealer profile for userId: {} email: {}",
                event.userId(), event.email());
    }
}
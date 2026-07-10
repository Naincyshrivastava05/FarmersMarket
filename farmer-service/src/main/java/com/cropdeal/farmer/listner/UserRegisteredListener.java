package com.cropdeal.farmer.listner;

import com.cropdeal.common.event.EventEnvelope;
import com.cropdeal.common.event.UserRegisteredEvent;
import com.cropdeal.farmer.model.Farmer;
import com.cropdeal.farmer.repository.FarmerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens for user.registered events and creates a farmer profile.
 * This class IS the async consumer from the design doc's event flow:
 * auth-service publishes -> RabbitMQ delivers -> this method runs.
 */
@Component
public class UserRegisteredListener {

    private static final Logger log =
            LoggerFactory.getLogger(UserRegisteredListener.class);

    private final FarmerRepository farmerRepository;

    public UserRegisteredListener(FarmerRepository farmerRepository) {
        this.farmerRepository = farmerRepository;
    }

    @RabbitListener(queues = "farmer.user.registered")
    public void handleUserRegistered(EventEnvelope<UserRegisteredEvent> envelope) {

        UserRegisteredEvent event = envelope.getPayload();

        // Only create a farmer profile if the registered user is a FARMER.
        // dealer-service handles DEALER registrations in its own listener.
        if (!"FARMER".equals(event.role())) {
            log.info("Skipping user.registered for role {}: {}",
                    event.role(), event.email());
            return;
        }

        // Idempotency check -- if this event is delivered twice (RabbitMQ
        // "at least once" guarantee), we don't create duplicate profiles.
        // We just skip it silently the second time.
        if (farmerRepository.existsByUserId(event.userId())) {
            log.warn("Farmer profile already exists for userId: {}",
                    event.userId());
            return;
        }

        Farmer farmer = new Farmer(event.userId(), event.email());
        farmerRepository.save(farmer);

        log.info("Created farmer profile for userId: {} email: {}",
                event.userId(), event.email());
    }
}
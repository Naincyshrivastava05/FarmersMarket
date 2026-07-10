package com.cropdeal.payment.repository;

import com.cropdeal.payment.model.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentEventRepository
        extends JpaRepository<PaymentEvent, UUID> {

    // Returns all events for an order in sequence order --
    // used to replay the event log and derive current state.
    List<PaymentEvent> findByOrderIdOrderBySequenceNoAsc(UUID orderId);

    // Used to get the next sequence number for a new event.
    Optional<PaymentEvent> findTopByOrderIdOrderBySequenceNoDesc(
            UUID orderId);
}
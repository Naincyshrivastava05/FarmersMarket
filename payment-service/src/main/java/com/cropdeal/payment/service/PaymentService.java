package com.cropdeal.payment.service;

import com.cropdeal.common.event.EventEnvelope;
import com.cropdeal.common.event.EventTypes;
import com.cropdeal.common.event.PaymentCompletedEvent;
import com.cropdeal.common.event.PaymentFailedEvent;
import com.cropdeal.payment.dto.PaymentIntentResponse;
import com.cropdeal.payment.model.PaymentEvent;
import com.cropdeal.payment.model.PaymentEventType;
import com.cropdeal.payment.model.PaymentSnapshot;
import com.cropdeal.payment.repository.PaymentEventRepository;
import com.cropdeal.payment.repository.PaymentSnapshotRepository;
import com.stripe.model.PaymentIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);

    private final PaymentEventRepository paymentEventRepository;
    private final PaymentSnapshotRepository paymentSnapshotRepository;
    private final RabbitTemplate rabbitTemplate;
    private final StripeService stripeService;

    public PaymentService(
            PaymentEventRepository paymentEventRepository,
            PaymentSnapshotRepository paymentSnapshotRepository,
            RabbitTemplate rabbitTemplate,
            StripeService stripeService) {
        this.paymentEventRepository = paymentEventRepository;
        this.paymentSnapshotRepository = paymentSnapshotRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.stripeService = stripeService;
    }

    /**
     * Called when order.created event arrives.
     * Creates a Stripe PaymentIntent and stores an INITIATED event.
     * Returns the clientSecret so the frontend can complete payment.
     */
    @Transactional
    public PaymentIntentResponse initiatePayment(
            UUID orderId, BigDecimal amount) {

        // Stripe enforces a minimum charge equivalent to roughly $0.50 USD,
        // regardless of currency. Reject obviously-too-small amounts early
        // with a clear message instead of letting Stripe's raw exception
        // bubble up as an unexplained 500 error to the frontend.
        BigDecimal minimumAmount = new BigDecimal("50.00");
        if (amount.compareTo(minimumAmount) < 0) {
            throw new IllegalArgumentException(
                    "Order amount too small for payment processing. " +
                    "Minimum order value is ₹" + minimumAmount);
        }     try {
            UUID paymentId = UUID.randomUUID();

            // Create PaymentIntent with Stripe.
            PaymentIntent intent =
                    stripeService.createPaymentIntent(amount, orderId);

            // Append INITIATED event to our audit log.
            int nextSeq = getNextSequenceNo(orderId);
            PaymentEvent initiated = new PaymentEvent(
                    orderId, paymentId,
                    PaymentEventType.INITIATED,
                    amount,
                    "Stripe PaymentIntent created: " + intent.getId(),
                    nextSeq
            );
            paymentEventRepository.save(initiated);

            // Save snapshot.
            updateSnapshot(orderId, paymentId,
                    PaymentEventType.INITIATED,
                    amount, nextSeq);

            return new PaymentIntentResponse(
                    intent.getClientSecret(), intent.getId());

        } catch (Exception e) {
            log.error("Failed to create PaymentIntent for " +
                    "orderId: {}", orderId, e);
            throw new RuntimeException(
                    "Payment initiation failed: " + e.getMessage());
        }
    }

    /**
     * Called by the webhook handler when Stripe confirms payment
     * succeeded. Publishes payment.completed so order-service
     * transitions the order to CONFIRMED.
     */
    @Transactional
    public void handlePaymentSuccess(String stripePaymentIntentId,
                                      UUID orderId,
                                      BigDecimal amount) {
        UUID paymentId = UUID.randomUUID();

        PaymentEvent completed = new PaymentEvent(
                orderId, paymentId,
                PaymentEventType.COMPLETED,
                amount,
                "Stripe payment confirmed: " + stripePaymentIntentId,
                getNextSequenceNo(orderId)
        );
        paymentEventRepository.save(completed);
        updateSnapshot(orderId, paymentId,
                PaymentEventType.COMPLETED,
                amount, completed.getSequenceNo());

        PaymentCompletedEvent payload = new PaymentCompletedEvent(
                orderId, paymentId, amount,
                "STRIPE", Instant.now()
        );
        rabbitTemplate.convertAndSend(
                EventTypes.EXCHANGE,
                EventTypes.PAYMENT_COMPLETED,
                new EventEnvelope<>(EventTypes.PAYMENT_COMPLETED, payload)
        );
        log.info("Payment completed for orderId: {}", orderId);
    }

    /**
     * Called by the webhook handler when Stripe reports payment failed.
     */
    @Transactional
    public void handlePaymentFailure(String stripePaymentIntentId,
                                      UUID orderId,
                                      BigDecimal amount) {
        UUID paymentId = UUID.randomUUID();

        PaymentEvent failed = new PaymentEvent(
                orderId, paymentId,
                PaymentEventType.FAILED,
                amount,
                "Stripe payment failed: " + stripePaymentIntentId,
                getNextSequenceNo(orderId)
        );
        paymentEventRepository.save(failed);
        updateSnapshot(orderId, paymentId,
                PaymentEventType.FAILED,
                amount, failed.getSequenceNo());

        PaymentFailedEvent payload = new PaymentFailedEvent(
                orderId, paymentId,
                "Stripe payment declined",
                Instant.now()
        );
        rabbitTemplate.convertAndSend(
                EventTypes.EXCHANGE,
                EventTypes.PAYMENT_FAILED,
                new EventEnvelope<>(EventTypes.PAYMENT_FAILED, payload)
        );
        log.warn("Payment failed for orderId: {}", orderId);
    }

    public List<PaymentEvent> getPaymentHistory(UUID orderId) {
        return paymentEventRepository
                .findByOrderIdOrderBySequenceNoAsc(orderId);
    }

    public PaymentSnapshot getCurrentState(UUID orderId) {
        return paymentSnapshotRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No payment found for orderId: " + orderId));
    }

    private int getNextSequenceNo(UUID orderId) {
        return paymentEventRepository
                .findTopByOrderIdOrderBySequenceNoDesc(orderId)
                .map(e -> e.getSequenceNo() + 1)
                .orElse(1);
    }

    private void updateSnapshot(UUID orderId, UUID paymentId,
            PaymentEventType state,
            BigDecimal amount, int sequenceNo) {

// Re-fetch fresh every time -- don't rely on a stale Optional
// captured earlier in the same method call before the lazy
// .orElse() construction quietly built a throwaway object.
var existing = paymentSnapshotRepository.findById(orderId);

if (existing.isPresent()) {
// Row already exists -- mutate and save (this becomes
// an UPDATE, not an INSERT, so no primary key conflict).
PaymentSnapshot snapshot = existing.get();
snapshot.update(state, sequenceNo);
paymentSnapshotRepository.save(snapshot);
} else {
// No row yet -- this is genuinely the first event
// for this orderId, safe to INSERT.
try {
PaymentSnapshot snapshot = new PaymentSnapshot(
    orderId, paymentId, state, amount, sequenceNo);
paymentSnapshotRepository.save(snapshot);
} catch (org.springframework.dao.DataIntegrityViolationException e) {
// Rare race: another thread inserted the row between
// our findById check and this save. Fall back to update
// instead of crashing -- this makes the method safe to
// call concurrently from initiatePayment() and the
// webhook handler without any extra locking.
PaymentSnapshot snapshot = paymentSnapshotRepository
    .findById(orderId)
    .orElseThrow();
snapshot.update(state, sequenceNo);
paymentSnapshotRepository.save(snapshot);
}
}
}
}
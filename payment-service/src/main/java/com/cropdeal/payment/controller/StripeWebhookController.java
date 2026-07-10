package com.cropdeal.payment.controller;

import com.cropdeal.payment.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class StripeWebhookController {

    private static final Logger log =
            LoggerFactory.getLogger(StripeWebhookController.class);

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final PaymentService paymentService;

    public StripeWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(
                    payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature");
            return ResponseEntity.badRequest()
                    .body("Invalid signature");
        }

        try {
            switch (event.getType()) {

                case "payment_intent.succeeded" -> {
                    // Instead of trusting getDataObjectDeserializer()
                    // to fully reconstruct the object from the webhook
                    // payload (which can return empty depending on API
                    // version / event payload shape), extract just the
                    // PaymentIntent ID from the raw JSON and fetch the
                    // authoritative, fully-populated object directly
                    // from Stripe's API. This is more robust and is
                    // Stripe's own recommended pattern for production
                    // webhook handlers.
                    String intentId = extractPaymentIntentId(event);
                    PaymentIntent intent = PaymentIntent.retrieve(intentId);

                    UUID orderId = UUID.fromString(
                            intent.getMetadata().get("orderId"));
                    BigDecimal amount = BigDecimal.valueOf(
                            intent.getAmount()).divide(
                                    BigDecimal.valueOf(100));

                    paymentService.handlePaymentSuccess(
                            intent.getId(), orderId, amount);
                }

                case "payment_intent.payment_failed" -> {
                    String intentId = extractPaymentIntentId(event);
                    PaymentIntent intent = PaymentIntent.retrieve(intentId);

                    UUID orderId = UUID.fromString(
                            intent.getMetadata().get("orderId"));
                    BigDecimal amount = BigDecimal.valueOf(
                            intent.getAmount()).divide(
                                    BigDecimal.valueOf(100));

                    paymentService.handlePaymentFailure(
                            intent.getId(), orderId, amount);
                }

                default -> log.info(
                        "Unhandled Stripe event type: {}",
                        event.getType());
            }
        } catch (StripeException e) {
            log.error("Failed to process webhook for event {}: {}",
                    event.getId(), e.getMessage());
            // Return 200 anyway -- if our own lookup fails, retrying
            // the same webhook delivery won't fix a data problem on
            // our end, and we don't want Stripe to keep redelivering
            // forever. Log loudly instead so a human investigates.
            return ResponseEntity.ok(
                    "Webhook received but processing failed, logged for investigation");
        }

        return ResponseEntity.ok("Webhook processed");
    }

    /**
     * Pulls the PaymentIntent ID directly out of the raw event JSON
     * rather than relying on automatic object deserialization, which
     * can be empty for "thin" event payloads under certain Stripe
     * API versions.
     */
    private String extractPaymentIntentId(Event event) {
        return event.getDataObjectDeserializer()
                .getRawJson() != null
                ? extractIdFromRawJson(
                        event.getDataObjectDeserializer().getRawJson())
                : extractIdFromRawJson(event.toJson());
    }

    private String extractIdFromRawJson(String json) {
        // The PaymentIntent's own id field looks like:
        // "id": "pi_3To093PqI4I83QHY2yYduApQ"
        // We pull it out with a simple, well-anchored search rather
        // than parsing the entire nested JSON structure manually.
        int idIndex = json.indexOf("\"id\"");
        int colonIndex = json.indexOf(':', idIndex);
        int firstQuote = json.indexOf('"', colonIndex);
        int secondQuote = json.indexOf('"', firstQuote + 1);
        return json.substring(firstQuote + 1, secondQuote);
    }
}
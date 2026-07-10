package com.cropdeal.payment.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Handles all direct communication with the Stripe API.
 * Kept separate from PaymentService so Stripe-specific logic
 * is isolated -- if you ever switch payment providers, you
 * only touch this class.
 */
@Service
public class StripeService {

    private static final Logger log =
            LoggerFactory.getLogger(StripeService.class);

    /**
     * Creates a Stripe PaymentIntent for the given amount.
     *
     * A PaymentIntent represents your intent to collect payment
     * from a customer. Stripe tracks its lifecycle from creation
     * through confirmation. It returns a client_secret which the
     * frontend uses to complete the payment using Stripe.js --
     * the actual card details never touch your server.
     *
     * Amount is in paise (smallest currency unit) for INR --
     * so ₹25.50 becomes 2550 paise. Stripe requires integers.
     */
    public PaymentIntent createPaymentIntent(
            BigDecimal amount, UUID orderId) throws StripeException {

        // Convert rupees to paise -- multiply by 100.
        long amountInPaise = amount
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amountInPaise)
                        .setCurrency("inr")
                        // Metadata lets you attach custom data to the
                        // PaymentIntent -- storing orderId here means
                        // when Stripe sends a webhook, you can identify
                        // which CropDeal order it belongs to.
                        .putMetadata("orderId", orderId.toString())
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams
                                        .AutomaticPaymentMethods
                                        .builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();

        PaymentIntent intent = PaymentIntent.create(params);
        log.info("Created PaymentIntent {} for orderId {}",
                intent.getId(), orderId);
        return intent;
    }
}
package com.cropdeal.payment.dto;

/**
 * Sent back to the frontend after creating a PaymentIntent.
 * The frontend uses clientSecret with Stripe.js to render
 * the payment form and confirm the payment -- the card details
 * go directly from browser to Stripe, never through our server.
 */
public record PaymentIntentResponse(
        String clientSecret,
        String paymentIntentId
) {
}
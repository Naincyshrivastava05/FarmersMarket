package com.cropdeal.payment.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Initializes the Stripe SDK with our secret key at startup.
 * @PostConstruct means this runs once after Spring has injected
 * all the @Value fields -- if we set Stripe.apiKey in the
 * constructor, the @Value might not be injected yet.
 */
@Configuration
public class StripeConfig {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
}
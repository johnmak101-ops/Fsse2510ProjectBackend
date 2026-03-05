package com.fsse2510.fsse2510_project_backend.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Stripe Config
 * (see docs.stripe.com/checkout/embedded/quickstart?client=react&lang=java)
 * (see baeldung.com/java-stripe-api)
 */

@Configuration
public class StripeConfig {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }
}

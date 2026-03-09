package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.service.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
/**
 * Controller for handling external Stripe webhook events.
 * <p>
 * Receives asynchronous events from Stripe (e.g., checkout.session.completed)
 * to update transaction status and trigger post-payment logic.
 * </p>
 */
public class StripeWebhookController {

    private static final String STRIPE_SIGNATURE_HEADER = "Stripe-Signature";

    private final StripeWebhookService stripeWebhookService;

    /**
     * Handles incoming Stripe webhooks.
     * <p>
     * Endpoint: POST /webhooks/stripe
     * </p>
     *
     * @param payload         The raw JSON payload from Stripe.
     * @param signatureHeader The 'Stripe-Signature' header used for verification.
     */
    @PostMapping("/stripe")
    public void handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(name = STRIPE_SIGNATURE_HEADER, required = false) String signatureHeader) {
        stripeWebhookService.processWebhook(payload, signatureHeader);
    }
}

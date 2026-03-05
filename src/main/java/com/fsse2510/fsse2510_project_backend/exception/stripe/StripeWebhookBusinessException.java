package com.fsse2510.fsse2510_project_backend.exception.stripe;

public class StripeWebhookBusinessException extends RuntimeException {
    public StripeWebhookBusinessException(String message) {
        super(message);
    }
}

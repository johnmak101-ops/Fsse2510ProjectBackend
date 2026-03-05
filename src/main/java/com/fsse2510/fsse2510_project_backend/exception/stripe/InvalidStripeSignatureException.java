package com.fsse2510.fsse2510_project_backend.exception.stripe;

public class InvalidStripeSignatureException extends RuntimeException {
    public InvalidStripeSignatureException(String message) {
        super(message);
    }
}

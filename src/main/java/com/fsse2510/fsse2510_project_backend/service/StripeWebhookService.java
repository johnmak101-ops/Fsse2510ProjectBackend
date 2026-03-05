package com.fsse2510.fsse2510_project_backend.service;

public interface StripeWebhookService {
    void processWebhook(String payload, String signatureHeader);
}

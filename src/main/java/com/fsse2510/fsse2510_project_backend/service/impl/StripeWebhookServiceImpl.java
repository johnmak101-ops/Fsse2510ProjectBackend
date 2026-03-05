package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.exception.stripe.InvalidStripeSignatureException;
import com.fsse2510.fsse2510_project_backend.exception.stripe.StripeWebhookBusinessException;
import com.fsse2510.fsse2510_project_backend.service.StripeWebhookService;
import com.fsse2510.fsse2510_project_backend.service.TransactionService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripeWebhookServiceImpl implements StripeWebhookService {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookServiceImpl.class);

    private final TransactionService transactionService;

    @Value("${stripe.webhook.secret:}")
    private String stripeWebhookSecret;

    @Override
    public void processWebhook(String payload, String signatureHeader) {
        if (stripeWebhookSecret == null || stripeWebhookSecret.isBlank()) {
            logger.error("[StripeWebhook] stripe.webhook.secret is not configured");
            throw new StripeWebhookBusinessException("webhook secret not configured");
        }

        if (signatureHeader == null || signatureHeader.isBlank()) {
            logger.warn("[StripeWebhook] Missing Stripe-Signature header");
            throw new InvalidStripeSignatureException("missing signature");
        }

        final Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            logger.warn("[StripeWebhook] Invalid signature: {}", e.getMessage());
            throw new InvalidStripeSignatureException("invalid signature");
        }

        if (!"payment_intent.succeeded".equals(event.getType())
                && !"payment_intent.payment_failed".equals(event.getType())
                && !"payment_intent.canceled".equals(event.getType())
                && !"checkout.session.completed".equals(event.getType())) {
            return;
        }

        Object obj = event.getDataObjectDeserializer().getObject().orElse(null);

        if ("checkout.session.completed".equals(event.getType())) {
            if (obj instanceof Session session) {
                try {
                    String clientReferenceId = session.getClientReferenceId();
                    String paymentIntentId = session.getPaymentIntent();

                    if (clientReferenceId != null && paymentIntentId != null) {
                        Integer tid = Integer.valueOf(clientReferenceId);
                        String firebaseUid = session.getMetadata() != null ? session.getMetadata().get("uid") : null;

                        if (firebaseUid == null) {
                            logger.warn("[StripeWebhook] Uid missing in session metadata for tid {}", tid);
                            return;
                        }

                        transactionService.finishTransactionFromStripeCheckout(firebaseUid, tid, paymentIntentId);
                    }
                } catch (Exception e) {
                    logger.error("[StripeWebhook] Failed processing checkout session", e);
                    throw new StripeWebhookBusinessException("checkout processing failed");
                }
            }
            return;
        }

        if (!(obj instanceof PaymentIntent intent)) {
            logger.warn("[StripeWebhook] Unexpected data object type for event {}: {}", event.getType(), obj);
            return;
        }

        Map<String, String> metadata = intent.getMetadata();
        String tidStr = metadata != null ? metadata.get("tid") : null;
        String firebaseUid = metadata != null ? metadata.get("uid") : null;

        if (tidStr == null || tidStr.isBlank() || firebaseUid == null || firebaseUid.isBlank()) {
            logger.error("[StripeWebhook] Missing required metadata. intent={}, metadata={}", intent.getId(), metadata);
            throw new StripeWebhookBusinessException("missing metadata");
        }

        Integer tid;
        try {
            tid = Integer.valueOf(tidStr);
        } catch (NumberFormatException e) {
            logger.error("[StripeWebhook] Invalid tid metadata. tid={}, intent={}", tidStr, intent.getId());
            throw new StripeWebhookBusinessException("invalid metadata");
        }

        try {
            if ("payment_intent.succeeded".equals(event.getType())) {
                transactionService.finishTransactionFromStripeWebhook(
                        firebaseUid,
                        tid,
                        intent.getId(),
                        intent.getAmount(),
                        intent.getCurrency());
            } else if ("payment_intent.payment_failed".equals(event.getType())) {
                // Payment was attempted but rejected (e.g., card declined)
                transactionService.failTransactionFromStripeWebhook(
                        firebaseUid,
                        tid,
                        intent.getId());
            } else if ("payment_intent.canceled".equals(event.getType())) {
                // User abandoned the checkout — Stripe auto-expired the PaymentIntent
                // NOTE: intentId match NOT required — PENDING transactions may have null
                // intentId
                logger.info("[StripeWebhook] PaymentIntent {} canceled (user abandoned). Aborting TID={}",
                        intent.getId(), tid);
                transactionService.abortTransactionFromStripeWebhook(firebaseUid, tid);
            }
        } catch (Exception e) {
            logger.error("[StripeWebhook] Failed processing eventId={}, type={}, intentId={}",
                    event.getId(), event.getType(), intent.getId(), e);
            throw new StripeWebhookBusinessException("payment intent processing failed");
        }
    }
}

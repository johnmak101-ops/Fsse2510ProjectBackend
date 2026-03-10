package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.exception.ProviderException;
import com.fsse2510.fsse2510_project_backend.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.PaymentIntentCaptureParams;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.net.RequestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.fsse2510.fsse2510_project_backend.util.BusinessConstants.MONEY_ROUNDING;

@Service
public class StripeServiceImpl implements StripeService {
    private static final Logger logger = LoggerFactory.getLogger(StripeServiceImpl.class);

    @Value("${stripe.currency:hkd}")
    private String stripeCurrency;

    @Override
    public Session createCheckoutSession(Integer tid, FirebaseUserData firebaseUser, BigDecimal total,
            String successUrl, String cancelUrl) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setClientReferenceId(tid.toString())
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .setPaymentIntentData(
                            SessionCreateParams.PaymentIntentData.builder()
                                    .setCaptureMethod(SessionCreateParams.PaymentIntentData.CaptureMethod.MANUAL)
                                    .putMetadata("tid", tid.toString())
                                    .putMetadata("uid", firebaseUser.getFirebaseUid())
                                    .build())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(stripeCurrency)
                                                    .setUnitAmount(total.multiply(BigDecimal.valueOf(100))
                                                            .setScale(0, MONEY_ROUNDING).longValue())
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Order #" + tid)
                                                                    .build())
                                                    .build())
                                    .build())
                    .putMetadata("tid", tid.toString())
                    .putMetadata("uid", firebaseUser.getFirebaseUid())
                    .build();

            return Session.create(params);
        } catch (StripeException e) {
            logger.error("Stripe session creation failed for TID: {}. Code: {}, Msg: {}", tid, e.getCode(),
                    e.getMessage());
            throw new ProviderException("Payment provider session creation failed", e);
        }
    }

    @Override
    public Session retrieveSession(String sessionId) {
        try {
            return Session.retrieve(sessionId);
        } catch (StripeException e) {
            logger.error("Stripe session retrieval failed for SessionID: {}. Code: {}, Msg: {}", sessionId, e.getCode(),
                    e.getMessage());
            throw new ProviderException("Failed to retrieve payment session", e);
        }
    }

    @Override
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) {
        try {
            return PaymentIntent.retrieve(paymentIntentId);
        } catch (StripeException e) {
            logger.error("Stripe PaymentIntent retrieval failed for ID: {}. Code: {}, Msg: {}", paymentIntentId,
                    e.getCode(), e.getMessage());
            throw new ProviderException("Failed to retrieve payment intent", e);
        }
    }

    @Override
    public void capturePayment(PaymentIntent intent, Integer tid) {
        try {
            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey("capture_" + tid)
                    .build();
            intent.capture(PaymentIntentCaptureParams.builder().build(), options);
            logger.info("Stripe Payment Captured for TID: {}", tid);
        } catch (StripeException e) {
            logger.error("Stripe Payment Capture failed for TID: {}. Code: {}, Msg: {}", tid, e.getCode(),
                    e.getMessage());
            throw new ProviderException("Failed to capture payment", e);
        }
    }

    @Override
    public void cancelPayment(PaymentIntent intent, Integer tid) {
        try {
            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey("cancel_" + tid)
                    .build();
            intent.cancel(PaymentIntentCancelParams.builder().build(), options);
            logger.info("Stripe Payment Canceled/Released for TID: {}", tid);
        } catch (StripeException e) {
            // Handle duplicate cancellation gracefully (e.g., intent_already_canceled or
            // payment_intent_unexpected_state)
            String errorCode = e.getCode() != null ? e.getCode().toLowerCase() : "";
            String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

            if (errorCode.contains("cancel") || errorMsg.contains("cancel")
                    || "payment_intent_unexpected_state".equals(errorCode)) {
                logger.info(
                        "Stripe Payment already canceled or cannot be further canceled for TID: {}. Code: {}, Msg: {}",
                        tid, e.getCode(), e.getMessage());
                return; // Gracefully continue instead of throwing
            }

            logger.error("Failed to cancel Stripe Payment for TID: {}. Code: {}, Msg: {}", tid, e.getCode(),
                    e.getMessage());
            // We don't necessarily want to throw here to avoid masking the original
            // business error,
            // but for StripeService consistency we throw ProviderException.
            throw new ProviderException("Failed to cancel payment", e);
        }
    }
}

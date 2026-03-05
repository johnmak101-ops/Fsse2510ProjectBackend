package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;

import java.math.BigDecimal;

public interface StripeService {
    Session createCheckoutSession(Integer tid, FirebaseUserData firebaseUser, BigDecimal total, String successUrl,
            String cancelUrl);

    Session retrieveSession(String sessionId);

    PaymentIntent retrievePaymentIntent(String paymentIntentId);

    void capturePayment(PaymentIntent intent, Integer tid);

    void cancelPayment(PaymentIntent intent, Integer tid);
}

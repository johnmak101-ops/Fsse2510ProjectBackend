package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.payment.domainObject.response.PaymentResponseData;
import com.fsse2510.fsse2510_project_backend.data.transaction.domainObject.request.CreateTransactionRequestData;
import com.fsse2510.fsse2510_project_backend.data.transaction.domainObject.response.TransactionResponseData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TransactionService {

        @Transactional
        TransactionResponseData createTransaction(CreateTransactionRequestData requestData);

        List<TransactionResponseData> getTransactions(FirebaseUserData user);

        TransactionResponseData getTransactionById(FirebaseUserData user, Integer tid);

        PaymentResponseData preparePayment(FirebaseUserData user, Integer tid);

        TransactionResponseData finishTransaction(FirebaseUserData user, Integer tid, String sessionId);

        TransactionResponseData abortTransaction(FirebaseUserData user, Integer tid);

        TransactionResponseData updateStatusToProcessing(FirebaseUserData user, Integer tid);

        void finishTransactionFromStripeWebhook(String firebaseUid, Integer tid, String intentId,
                                                Long amount, String currency);

        void finishTransactionFromStripeCheckout(String firebaseUid, Integer tid, String intentId);

        void failTransactionFromStripeWebhook(String firebaseUid, Integer tid, String intentId);

        void abortTransactionFromStripeWebhook(String firebaseUid, Integer tid);

        Page<TransactionResponseData> getAllTransactions(Pageable pageable);

        TransactionResponseData getAdminTransactionById(Integer tid);

        TransactionResponseData adminUpdateTransactionStatus(Integer tid, String status);
}

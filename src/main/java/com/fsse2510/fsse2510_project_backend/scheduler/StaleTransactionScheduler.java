package com.fsse2510.fsse2510_project_backend.scheduler;

import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.data.transaction.entity.TransactionEntity;
import com.fsse2510.fsse2510_project_backend.data.transaction.status.PaymentStatus;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.repository.TransactionRepository;
import com.fsse2510.fsse2510_project_backend.service.StripeService;
import com.fsse2510.fsse2510_project_backend.service.impl.TransactionFinalizationService;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StaleTransactionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(StaleTransactionScheduler.class);
    private static final int STALE_THRESHOLD_HOURS = 2;

    private final TransactionRepository transactionRepository;
    private final StripeService stripeService;
    private final TransactionFinalizationService finalizationService;

    /**
     * Scans for transactions stuck in PROCESSING for over 2 hours
     * and attempts automated reconciliation against Stripe.
     * Runs every 30 minutes.
     */
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void detectStaleProcessingTransactions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(STALE_THRESHOLD_HOURS);
        List<TransactionEntity> stale = transactionRepository
                .findAllByStatusAndDatetimeBefore(PaymentStatus.PROCESSING, cutoff);

        if (stale.isEmpty()) {
            return;
        }

        logger.warn("=== STALE TRANSACTION ALERT: {} transaction(s) stuck in PROCESSING for over {} hours ===",
                stale.size(), STALE_THRESHOLD_HOURS);

        for (TransactionEntity tx : stale) {
            reconcile(tx);
        }
    }

    private void reconcile(TransactionEntity tx) {
        Integer tid = tx.getTid();
        String intentId = tx.getStripePaymentIntentId();

        if (intentId == null || tx.getUser() == null) {
            logger.warn("[Reconcile] TID={} has no Stripe intent or user, marking FAILED and recovering cart", tid);
            markFailedAndRecover(tx);
            return;
        }

        try {
            PaymentIntent intent = stripeService.retrievePaymentIntent(intentId);
            String status = intent.getStatus();
            logger.info("[Reconcile] TID={}, Stripe status={}", tid, status);

            switch (status) {
                case "succeeded" -> finalizeSucceeded(tx);
                case "requires_capture" -> captureAndFinalize(tx, intent);
                case "canceled", "requires_payment_method" -> {
                    logger.info("[Reconcile] TID={} payment not completed ({}), marking FAILED", tid, status);
                    markFailedAndRecover(tx);
                }
                default -> logger.warn("[Reconcile] TID={} has unhandled Stripe status '{}', skipping", tid, status);
            }
        } catch (Exception e) {
            logger.error("[Reconcile] TID={} failed to query Stripe: {}", tid, e.getMessage());
        }
    }

    private void finalizeSucceeded(TransactionEntity tx) {
        try {
            FirebaseUserData firebaseUser = new FirebaseUserData(
                    tx.getUser().getFirebaseUid(), null);
            MembershipLevel prefLevel = tx.getUser().getLevel();
            finalizationService.finalizeSuccess(firebaseUser, tx.getTid(), prefLevel);
            logger.info("[Reconcile] TID={} finalized successfully", tx.getTid());
        } catch (Exception e) {
            logger.error("[Reconcile] TID={} finalization failed: {}", tx.getTid(), e.getMessage());
        }
    }

    private void captureAndFinalize(TransactionEntity tx, PaymentIntent intent) {
        try {
            stripeService.capturePayment(intent, tx.getTid());
            finalizeSucceeded(tx);
        } catch (Exception e) {
            logger.error("[Reconcile] TID={} capture failed: {}", tx.getTid(), e.getMessage());
        }
    }

    private void markFailedAndRecover(TransactionEntity tx) {
        try {
            tx.setStatus(PaymentStatus.FAILED);
            transactionRepository.save(tx);
            finalizationService.recoverCartItems(tx);
            logger.info("[Reconcile] TID={} marked FAILED, cart items recovered", tx.getTid());
        } catch (Exception e) {
            logger.error("[Reconcile] TID={} recovery failed: {}", tx.getTid(), e.getMessage());
        }
    }
}

package com.fsse2510.fsse2510_project_backend.scheduler;

import com.fsse2510.fsse2510_project_backend.data.transaction.entity.TransactionEntity;
import com.fsse2510.fsse2510_project_backend.data.transaction.status.PaymentStatus;
import com.fsse2510.fsse2510_project_backend.repository.TransactionRepository;
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

    /**
     * Scans for transactions stuck in PROCESSING for over 2 hours.
     * These may indicate a captured payment where DB finalization failed.
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
            logger.warn("Stale TID={}, User={}, Total={}, Created={}, StripeIntent={}",
                    tx.getTid(),
                    tx.getUser() != null ? tx.getUser().getUid() : "unknown",
                    tx.getTotal(),
                    tx.getDatetime(),
                    tx.getStripePaymentIntentId() != null ? tx.getStripePaymentIntentId() : "none");
        }
    }
}

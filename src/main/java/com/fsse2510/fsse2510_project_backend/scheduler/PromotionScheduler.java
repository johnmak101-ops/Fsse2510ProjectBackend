package com.fsse2510.fsse2510_project_backend.scheduler;

import com.fsse2510.fsse2510_project_backend.service.PromotionProductSyncService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PromotionScheduler.class);

    private final PromotionProductSyncService promotionProductSyncService;

    /**
     * Daily job to clear expired promotions from products.
     * Runs at 2:00 AM every day.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void clearExpiredPromotions() {
        logger.info("=== Starting scheduled clearExpiredPromotions job ===");
        try {
            promotionProductSyncService.clearExpiredPromotions();
            logger.info("=== Completed clearExpiredPromotions job successfully ===");
        } catch (Exception e) {
            logger.error("=== clearExpiredPromotions job failed ===", e);
        }
    }
}

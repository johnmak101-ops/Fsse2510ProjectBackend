package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
import com.fsse2510.fsse2510_project_backend.repository.ProductRepository;
import com.fsse2510.fsse2510_project_backend.repository.PromotionRepository;
import com.fsse2510.fsse2510_project_backend.service.PromotionApplicabilityService;
import com.fsse2510.fsse2510_project_backend.service.PromotionProductSyncService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionProductSyncServiceImpl implements PromotionProductSyncService {

    private static final Logger logger = LoggerFactory.getLogger(PromotionProductSyncServiceImpl.class);

    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;
    private final PromotionApplicabilityService promotionApplicabilityService;
    private final PromotionCalculator promotionCalculator;

    /**
     * Apply promotion to all matching products asynchronously.
     * Updates isSale flag and promotion tracking fields.
     */
    @Async
    @Transactional
    @Override
    public void applyPromotionToProductsAsync(PromotionEntity promotion) {
        logger.info("Starting async application of promotion: id={}", promotion.getId());

        // Find all matching products
        List<ProductEntity> matchingProducts = findMatchingProducts(promotion);
        logger.info("Found {} matching products for promotion: id={}", matchingProducts.size(), promotion.getId());

        if (matchingProducts.isEmpty()) {
            logger.warn("No products match promotion: id={}", promotion.getId());
            return;
        }

        // Process in batches to avoid memory issues
        int batchSize = 100;
        for (int i = 0; i < matchingProducts.size(); i += batchSize) {
            int end = Math.min(i + batchSize, matchingProducts.size());
            List<ProductEntity> batch = matchingProducts.subList(i, end);

            for (ProductEntity product : batch) {
                applyPromotionToProduct(product, promotion);
            }

            // Batch save
            productRepository.saveAll(batch);
            logger.info("Updated batch {} to {} for promotion: id={}", i, end, promotion.getId());
        }

        // Clear cache
        clearProductCache();
        logger.info("Completed async application of promotion: id={}", promotion.getId());
    }

    /**
     * Remove promotion from all affected products asynchronously.
     */
    @Async
    @Transactional
    @Override
    public void removePromotionFromProductsAsync(Integer promotionId) {
        logger.info("Starting async removal of promotion: id={}", promotionId);

        List<ProductEntity> affectedProducts = productRepository.findByPromotionId(promotionId);
        if (affectedProducts.isEmpty())
            return;

        List<PromotionEntity> activePromotions = promotionRepository.findActivePromotionsWithTargets(LocalDateTime.now())
                .stream()
                .filter(p -> !p.getId().equals(promotionId))
                .toList();

        for (ProductEntity product : affectedProducts) {
            removePromotionFromProduct(product);
            findAndApplyNextBestPromotion(product, activePromotions);
        }

        productRepository.saveAll(affectedProducts);
        clearProductCache();
        logger.info("Completed async removal of promotion: id={}", promotionId);
    }

    @Transactional
    @Override
    public void removePromotionFromProductsSync(Integer promotionId) {
        logger.info("Starting sync removal of promotion: id={}", promotionId);

        List<PromotionEntity> activePromotions = promotionRepository
                .findActivePromotionsWithTargets(LocalDateTime.now())
                .stream()
                .filter(p -> !p.getId().equals(promotionId))
                .toList();

        removePromotionFromProductsSync(promotionId, activePromotions);

        clearProductCache();
        logger.info("Completed sync removal of promotion: id={}", promotionId);
    }

    /**
     * Clear expired promotions (called by scheduler).
     */
    @Transactional
    @Override
    public void clearExpiredPromotions() {
        logger.info("Starting clearExpiredPromotions job");

        LocalDateTime now = LocalDateTime.now();
        List<PromotionEntity> expiredPromotions = promotionRepository.findExpiredPromotions(now);
        List<PromotionEntity> activePromotions = promotionRepository.findActivePromotionsWithTargets(now);

        logger.info("Found {} expired promotions", expiredPromotions.size());

        for (PromotionEntity promotion : expiredPromotions) {
            removePromotionFromProductsSync(promotion.getId(), activePromotions);
        }

        clearProductCache();
        logger.info("Completed clearExpiredPromotions job");
    }

    // ============ Private Helper Methods ============

    private List<ProductEntity> findMatchingProducts(PromotionEntity promotion) {
        // Get all products with essential data for applicability matching
        // (Category, Collection, and Tags via @BatchSize)
        List<ProductEntity> allProducts = productRepository.findAllWithPromotionEssentials();

        // Filter by promotion applicability (using product eligibility check for sync)
        return allProducts.stream()
                .filter(product -> promotionApplicabilityService.isProductEligibleForPromotion(promotion, product))
                .toList();
    }

    private void applyPromotionToProduct(ProductEntity product, PromotionEntity promotion) {
        // Check if product already has a promotion
        if (product.getPromotion() != null) {
            // Compare discounts, keep better one
            PromotionEntity existingPromo = product.getPromotion();
            if (isBetterDiscount(existingPromo, promotion, product.getPrice())) {
                logger.debug("Keeping existing better promotion for product: pid={}", product.getPid());
                return;
            }
        }

        // Apply new promotion
        product.setIsSale(true);
        product.setPromotion(promotion);
        product.setPromotionBadgeText(promotionCalculator.generateBadgeText(promotion));
        logger.debug("Applied promotion to product: pid={}, promotion={}", product.getPid(), promotion.getId());
    }

    private void removePromotionFromProduct(ProductEntity product) {
        product.setIsSale(false);
        product.setPromotion(null);
        product.setPromotionBadgeText(null);
    }

    private void removePromotionFromProductsSync(Integer promotionId, List<PromotionEntity> activePromotions) {
        List<ProductEntity> affectedProducts = productRepository.findByPromotionId(promotionId);
        for (ProductEntity product : affectedProducts) {
            removePromotionFromProduct(product);
            findAndApplyNextBestPromotion(product, activePromotions);
        }
        productRepository.saveAll(affectedProducts);
    }

    private void findAndApplyNextBestPromotion(ProductEntity product, List<PromotionEntity> activePromotions) {
        PromotionEntity bestCandidate = null;

        for (PromotionEntity candidate : activePromotions) {
            // Check if this candidate applies to the product (sync targeting)
            if (promotionApplicabilityService.isProductEligibleForPromotion(candidate, product)) {
                if (bestCandidate == null) {
                    bestCandidate = candidate;
                } else {
                    // Compare with current best
                    if (isBetterDiscount(candidate, bestCandidate, product.getPrice())) {
                        bestCandidate = candidate;
                    }
                }
            }
        }

        if (bestCandidate != null) {
            applyPromotionToProduct(product, bestCandidate);
            logger.debug("Fallback: Applied next best promotion id={} to product pid={}",
                    bestCandidate.getId(), product.getPid());
        }
    }

    private boolean isBetterDiscount(PromotionEntity existing, PromotionEntity newPromo, BigDecimal originalPrice) {
        BigDecimal existingDiscount = promotionCalculator.calculateDiscountAmount(existing, originalPrice, null);
        BigDecimal newDiscount = promotionCalculator.calculateDiscountAmount(newPromo, originalPrice, null);
        return existingDiscount.compareTo(newDiscount) > 0;
    }

    @CacheEvict(value = { "product_v8", "product_recommendations_v8",
            "product_attributes_v8", "product_showcase_v5" }, allEntries = true)
    public void clearProductCache() {
        logger.debug("Cleared product cache");
    }
}

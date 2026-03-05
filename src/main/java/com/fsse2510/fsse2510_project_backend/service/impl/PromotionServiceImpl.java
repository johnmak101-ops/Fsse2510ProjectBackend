package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import com.fsse2510.fsse2510_project_backend.data.promotion.domainObject.request.CreatePromotionRequestData;
import com.fsse2510.fsse2510_project_backend.data.promotion.domainObject.request.UpdatePromotionRequestData;
import com.fsse2510.fsse2510_project_backend.data.promotion.domainObject.response.PromotionResponseData;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.promotion.promotionType.PromotionType;
import com.fsse2510.fsse2510_project_backend.exception.product.ProductNotFoundException;
import com.fsse2510.fsse2510_project_backend.mapper.promotion.PromotionDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.promotion.PromotionEntityMapper;
import com.fsse2510.fsse2510_project_backend.exception.promotion.PromotionNotFoundException;
import com.fsse2510.fsse2510_project_backend.exception.promotion.PromotionValidationException;
import com.fsse2510.fsse2510_project_backend.repository.ProductRepository;
import com.fsse2510.fsse2510_project_backend.repository.PromotionRepository;
import com.fsse2510.fsse2510_project_backend.service.CartPromotionEnricherService;
import com.fsse2510.fsse2510_project_backend.service.ProductPromotionEnricherService;
import com.fsse2510.fsse2510_project_backend.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fsse2510.fsse2510_project_backend.service.PromotionProductSyncService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private static final Logger logger = LoggerFactory.getLogger(PromotionServiceImpl.class);

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final PromotionEntityMapper promotionEntityMapper;
    private final PromotionDataMapper promotionDataMapper;
    private final PromotionProductSyncService promotionProductSyncService;
    private final CartPromotionEnricherService cartPromotionEnricherService;
    private final ProductPromotionEnricherService productPromotionEnricherService;

    @Override
    @Transactional
    public PromotionResponseData createPromotion(CreatePromotionRequestData requestData) {
        logger.info("Creating promotion: name={}", requestData.getName());

        // Basic Validation
        validatePromotionRequest(requestData.getStartDate(), requestData.getEndDate(),
                requestData.getType(), requestData.getDiscountType(), requestData.getDiscountValue(),
                requestData.getBuyX(), requestData.getGetY(),
                requestData.getTargetPids(), requestData.getTargetCategories(),
                requestData.getTargetCollections(), requestData.getTargetTags());

        // Convert and save promotion
        PromotionEntity entity = promotionEntityMapper.toEntity(requestData);
        PromotionEntity savedEntity = promotionRepository.save(entity);

        // Clear cache IMMEDIATELY (sync) before async to prevent stale data
        promotionProductSyncService.clearProductCache();
        cartPromotionEnricherService.clearCache();
        productPromotionEnricherService.clearCache();

        promotionProductSyncService.applyPromotionToProductsAsync(savedEntity);
        logger.info("Triggered async product sync for promotion: id={}", savedEntity.getId());

        return promotionDataMapper.toResponseData(savedEntity);
    }

    @Override
    public List<PromotionResponseData> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(promotionDataMapper::toResponseData)
                .toList();
    }

    @Override
    public PromotionResponseData getPromotionById(Integer id) {
        PromotionEntity entity = promotionRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Get Promotion Failed: Not found. ID={}", id);
                    return new PromotionNotFoundException(id);
                });
        return promotionDataMapper.toResponseData(entity);
    }

    @Override
    @Transactional
    public PromotionResponseData updatePromotion(Integer id, UpdatePromotionRequestData requestData) {
        logger.info("Updating promotion: id={}", id);

        // Basic Validation
        validatePromotionRequest(requestData.getStartDate(), requestData.getEndDate(),
                requestData.getType(), requestData.getDiscountType(), requestData.getDiscountValue(),
                requestData.getBuyX(), requestData.getGetY(),
                requestData.getTargetPids(), requestData.getTargetCategories(),
                requestData.getTargetCollections(), requestData.getTargetTags());

        PromotionEntity existingEntity = promotionRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Update Promotion Failed: Not found. ID={}", id);
                    return new PromotionNotFoundException(id);
                });

        // Clear cache IMMEDIATELY (sync) before async operations
        promotionProductSyncService.clearProductCache();
        cartPromotionEnricherService.clearCache();
        productPromotionEnricherService.clearCache();

        // Remove old promotion from products sync
        promotionProductSyncService.removePromotionFromProductsSync(id);

        // Use @MappingTarget to update the existing Entity
        promotionEntityMapper.updateEntity(requestData, existingEntity);

        PromotionEntity savedEntity = promotionRepository.save(existingEntity);

        // Re-apply updated promotion async
        promotionProductSyncService.applyPromotionToProductsAsync(savedEntity);
        logger.info("Triggered async re-sync for updated promotion: id={}", savedEntity.getId());

        return promotionDataMapper.toResponseData(savedEntity);
    }

    @Override
    @Transactional
    public void deletePromotion(Integer id) {
        logger.info("Deleting promotion: id={}", id);

        if (!promotionRepository.existsById(id)) {
            logger.warn("Delete Promotion Failed: Not found. ID={}", id);
            throw new PromotionNotFoundException(id);
        }

        // Clear cache IMMEDIATELY (sync) - user will see updated data on refresh
        promotionProductSyncService.clearProductCache();
        cartPromotionEnricherService.clearCache();
        productPromotionEnricherService.clearCache();
        logger.info("Cache cleared for promotion deletion: id={}", id);

        // Remove promotion from products first (sync) to avoid FK constraint issues
        promotionProductSyncService.removePromotionFromProductsSync(id);
        logger.info("Sync product cleanup completed for deleted promotion: id={}", id);

        // Delete promotion
        promotionRepository.deleteById(id);
        logger.info("Deleted promotion: id={}", id);
    }

    @Override
    @Transactional
    public void assignPromotionToProduct(Integer promoId, Integer pid) {
        ProductEntity product = productRepository.findById(pid)
                .orElseThrow(() -> new ProductNotFoundException(pid));

        PromotionEntity promotion = promotionRepository.findById(promoId)
                .orElseThrow(() -> new PromotionNotFoundException(promoId));

        product.setPromotion(promotion);
        productRepository.save(product);
        logger.info("Assigned Promotion ID={} to Product ID={}", promoId, pid);
    }

    private void validatePromotionRequest(LocalDateTime start, LocalDateTime end, PromotionType type,
            DiscountType discountType, java.math.BigDecimal discountValue,
            Integer buyX, Integer getY,
            java.util.Collection<Integer> pids, java.util.Collection<String> categories,
            java.util.Collection<String> collections, java.util.Collection<String> tags) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new PromotionValidationException("Start date must be before end date.");
        }

        if (type == PromotionType.BUY_X_GET_Y_FREE) {
            if (buyX == null || buyX < 1 || getY == null || getY < 1) {
                throw new PromotionValidationException("Buy X and Get Y must be at least 1.");
            }
        } else {
            if (discountType == null) {
                throw new PromotionValidationException("Discount type is required.");
            }
            if (discountValue == null) {
                throw new PromotionValidationException("Discount value is required.");
            }
            if (discountType == DiscountType.PERCENTAGE) {
                if (discountValue.compareTo(java.math.BigDecimal.ZERO) <= 0 ||
                        discountValue.compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
                    throw new PromotionValidationException(
                            "Percentage discount must be between 1 and 100.");
                }
            } else if (discountType == DiscountType.FIXED) {
                if (discountValue.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    throw new PromotionValidationException("Fixed discount must be greater than 0.");
                }
            }
        }

        switch (type) {
            case SPECIFIC_PRODUCT_DISCOUNT -> {
                if (pids == null || pids.isEmpty())
                    throw new PromotionValidationException("At least one product must be selected.");
            }
            case SPECIFIC_CATEGORY_DISCOUNT -> {
                if (categories == null || categories.isEmpty())
                    throw new PromotionValidationException("At least one category must be selected.");
            }
            case SPECIFIC_COLLECTION_DISCOUNT -> {
                if (collections == null || collections.isEmpty())
                    throw new PromotionValidationException("At least one collection must be selected.");
            }
            case SPECIFIC_TAG_DISCOUNT -> {
                if (tags == null || tags.isEmpty())
                    throw new PromotionValidationException("At least one tag must be selected.");
            }
            default -> {
            }
        }
    }

    @Override
    public List<PromotionResponseData> getActivePromotionsByType(List<PromotionType> types) {
        // Currently, fetch all matching Types; expiry check is handled by the
        // Calculator
        return promotionRepository.findByTypeIn(types).stream()
                .map(promotionDataMapper::toResponseData)
                .toList();
    }

    @Override
    public List<PromotionResponseData> getActivePublicPromotions(List<PromotionType> types) {
        LocalDateTime now = LocalDateTime.now();
        List<PromotionEntity> active = (types == null || types.isEmpty())
                ? promotionRepository.findActivePromotions(now)
                : promotionRepository.findActiveByType(types, now);
        return active.stream()
                .map(promotionDataMapper::toResponseData)
                .toList();
    }
}
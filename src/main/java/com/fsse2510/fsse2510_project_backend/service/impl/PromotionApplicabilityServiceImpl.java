package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import com.fsse2510.fsse2510_project_backend.data.promotion.cache.CachedPromotion;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
import com.fsse2510.fsse2510_project_backend.data.promotion.promotionType.PromotionType;
import com.fsse2510.fsse2510_project_backend.service.PromotionApplicabilityService;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class PromotionApplicabilityServiceImpl implements PromotionApplicabilityService {

    private static final Logger logger = LoggerFactory.getLogger(PromotionApplicabilityServiceImpl.class);

    // ========== PromotionEntity overloads ==========

    @Override
    public boolean isApplicable(PromotionEntity promo, ProductEntity product, boolean isStrict) {
        return isApplicable(promo, product, null, isStrict);
    }

    @Override
    public boolean isApplicable(PromotionEntity promo, ProductEntity product, UserEntity user, boolean isStrict) {
        if (promo == null || product == null)
            return false;
        return isApplicableInternal(
                promo.getId(), promo.getType(), promo.getStartDate(), promo.getEndDate(),
                promo.getTargetMemberLevel(),
                promo.getTargetPids(), promo.getTargetCategories(),
                promo.getTargetCollections(), promo.getTargetTags(),
                product, user, isStrict);
    }

    @Override
    public boolean isProductEligibleForPromotion(PromotionEntity promo, ProductEntity product) {
        if (promo == null || product == null)
            return false;
        return isProductEligibleInternal(
                promo.getType(),
                promo.getTargetPids(), promo.getTargetCategories(),
                promo.getTargetCollections(), promo.getTargetTags(),
                product);
    }

    // ========== CachedPromotion overloads ==========

    @Override
    public boolean isApplicable(CachedPromotion promo, ProductEntity product, boolean isStrict) {
        return isApplicable(promo, product, null, isStrict);
    }

    @Override
    public boolean isApplicable(CachedPromotion promo, ProductEntity product, UserEntity user, boolean isStrict) {
        if (promo == null || product == null)
            return false;
        return isApplicableInternal(
                promo.id(), promo.type(), promo.startDate(), promo.endDate(),
                promo.targetMemberLevel(),
                promo.targetPids(), promo.targetCategories(),
                promo.targetCollections(), promo.targetTags(),
                product, user, isStrict);
    }

    @Override
    public boolean isProductEligibleForPromotion(CachedPromotion promo, ProductEntity product) {
        if (promo == null || product == null)
            return false;
        return isProductEligibleInternal(
                promo.type(),
                promo.targetPids(), promo.targetCategories(),
                promo.targetCollections(), promo.targetTags(),
                product);
    }

    // ========== Shared implementation ==========

    private boolean isApplicableInternal(
            Integer promoId, PromotionType type,
            LocalDateTime startDate, LocalDateTime endDate,
            MembershipLevel targetMemberLevel,
            Set<Integer> targetPids, Set<String> targetCategories,
            Set<String> targetCollections, Set<String> targetTags,
            ProductEntity product, UserEntity user, boolean isStrict) {

        LocalDateTime now = LocalDateTime.now();
        if (startDate.isAfter(now) || (endDate != null && endDate.isBefore(now))) {
            return false;
        }

        if (targetMemberLevel != null) {
            if (isStrict) {
                if (user == null || !isUserLevelQualified(targetMemberLevel, user.getLevel())) {
                    logger.debug("Promotion {} strictly requires level {}, user qualification failed",
                            promoId, targetMemberLevel);
                    return false;
                }
            } else {
                if (user == null) {
                    logger.debug("Promotion {} has target level {}, allowing for guest (teaser mode)",
                            promoId, targetMemberLevel);
                } else if (!isUserLevelQualified(targetMemberLevel, user.getLevel())) {
                    logger.debug(
                            "Promotion {} has target level {}, user level is {}, allowing (showing teaser benefits)",
                            promoId, targetMemberLevel, user.getLevel());
                }
            }
        }

        return isProductEligibleInternal(type, targetPids, targetCategories, targetCollections, targetTags, product);
    }

    private boolean isProductEligibleInternal(
            PromotionType type,
            Set<Integer> targetPids, Set<String> targetCategories,
            Set<String> targetCollections, Set<String> targetTags,
            ProductEntity product) {

        switch (type) {
            case STOREWIDE_SALE:
                return true;

            case SPECIFIC_PRODUCT_DISCOUNT:
                return matchesPid(targetPids, product);

            case SPECIFIC_CATEGORY_DISCOUNT:
                return matchesCategory(targetCategories, product);

            case SPECIFIC_COLLECTION_DISCOUNT:
                return matchesCollection(targetCollections, product);

            case SPECIFIC_TAG_DISCOUNT:
                return matchesTag(targetTags, product);

            case MEMBERSHIP_DISCOUNT:
            case BUY_X_GET_Y_FREE:
            case BUNDLE_DISCOUNT:
            case MIN_QUANTITY_DISCOUNT:
            case MIN_AMOUNT_DISCOUNT: {
                boolean hasTargets = !targetPids.isEmpty()
                        || !targetCategories.isEmpty()
                        || !targetCollections.isEmpty()
                        || !targetTags.isEmpty();

                if (!hasTargets)
                    return true;

                return matchesPid(targetPids, product)
                        || matchesCategory(targetCategories, product)
                        || matchesCollection(targetCollections, product)
                        || matchesTag(targetTags, product);
            }

            default:
                return false;
        }
    }

    private boolean matchesPid(Set<Integer> targetPids, ProductEntity product) {
        if (targetPids.isEmpty())
            return false;
        return targetPids.contains(product.getPid());
    }

    private boolean matchesCategory(Set<String> targetCategories, ProductEntity product) {
        if (targetCategories.isEmpty() || product.getCategory() == null)
            return false;
        String productCategory = product.getCategory().getName().trim();
        return targetCategories.stream()
                .anyMatch(target -> target.trim().equalsIgnoreCase(productCategory));
    }

    private boolean matchesCollection(Set<String> targetCollections, ProductEntity product) {
        if (targetCollections.isEmpty() || product.getCollection() == null)
            return false;
        String productCollection = product.getCollection().getName().trim();
        return targetCollections.stream()
                .anyMatch(target -> target.trim().equalsIgnoreCase(productCollection));
    }

    private boolean matchesTag(Set<String> targetTags, ProductEntity product) {
        if (targetTags.isEmpty() || product.getTags().isEmpty())
            return false;

        return product.getTags().stream()
                .map(String::trim)
                .anyMatch(pTag -> targetTags.stream()
                        .map(String::trim)
                        .anyMatch(tTag -> tTag.equalsIgnoreCase(pTag)));
    }

    private boolean isUserLevelQualified(MembershipLevel targetLevel, MembershipLevel userLevel) {
        if (targetLevel == null) {
            return true;
        }
        if (userLevel == null) {
            return false;
        }

        boolean qualified = userLevel.getRank() >= targetLevel.getRank();
        logger.debug("Level check: user={}, target={}, result={}",
                userLevel, targetLevel, qualified);
        return qualified;
    }
}

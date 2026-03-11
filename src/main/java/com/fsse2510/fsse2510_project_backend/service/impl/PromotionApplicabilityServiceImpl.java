package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
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

    @Override
    public boolean isApplicable(PromotionEntity promo, ProductEntity product, boolean isStrict) {
        return isApplicable(promo, product, null, isStrict);
    }

    @Override
    public boolean isApplicable(PromotionEntity promo, ProductEntity product, UserEntity user, boolean isStrict) {
        if (promo == null || product == null)
            return false;

        LocalDateTime now = LocalDateTime.now();
        if (promo.getStartDate().isAfter(now) || (promo.getEndDate() != null && promo.getEndDate().isBefore(now))) {
            return false;
        }

        if (promo.getTargetMemberLevel() != null) {
            if (isStrict) {
                // Strict Mode: Actual Transaction Enforcement
                if (user == null || !isUserLevelQualified(promo.getTargetMemberLevel(), user.getLevel())) {
                    logger.debug("Promotion {} strictly requires level {}, user qualification failed",
                            promo.getId(), promo.getTargetMemberLevel());
                    return false;
                }
            } else {
                // Teaser Mode: Display for everyone
                if (user == null) {
                    logger.debug("Promotion {} has target level {}, allowing for guest (teaser mode)",
                            promo.getId(), promo.getTargetMemberLevel());
                } else if (!isUserLevelQualified(promo.getTargetMemberLevel(), user.getLevel())) {
                    logger.debug(
                            "Promotion {} has target level {}, user level is {}, allowing (showing teaser benefits)",
                            promo.getId(), promo.getTargetMemberLevel(), user.getLevel());
                }
                // Continue to check product targeting (Step 3)
            }
        }

        return isProductEligibleForPromotion(promo, product);
    }

    @Override
    public boolean isProductEligibleForPromotion(PromotionEntity promo, ProductEntity product) {
        if (promo == null || product == null)
            return false;

        switch (promo.getType()) {
            case STOREWIDE_SALE:
                return true;

            case SPECIFIC_PRODUCT_DISCOUNT:
                return matchesPid(promo, product);

            case SPECIFIC_CATEGORY_DISCOUNT:
                return matchesCategory(promo, product);

            case SPECIFIC_COLLECTION_DISCOUNT:
                return matchesCollection(promo, product);

            case SPECIFIC_TAG_DISCOUNT:
                return matchesTag(promo, product);

            case MEMBERSHIP_DISCOUNT:
            case BUY_X_GET_Y_FREE:
            case BUNDLE_DISCOUNT:
            case MIN_QUANTITY_DISCOUNT:
            case MIN_AMOUNT_DISCOUNT: {
                boolean hasTargets = !isEmpty(promo.getTargetPids())
                        || !isEmpty(promo.getTargetCategories())
                        || !isEmpty(promo.getTargetCollections())
                        || !isEmpty(promo.getTargetTags());

                if (!hasTargets)
                    return true;

                return matchesPid(promo, product)
                        || matchesCategory(promo, product)
                        || matchesCollection(promo, product)
                        || matchesTag(promo, product);
            }

            default:
                return false;
        }
    }

    private boolean matchesPid(PromotionEntity promo, ProductEntity product) {
        if (isEmpty(promo.getTargetPids()))
            return false;
        return promo.getTargetPids().contains(product.getPid());
    }

    private boolean matchesCategory(PromotionEntity promo, ProductEntity product) {
        if (isEmpty(promo.getTargetCategories()) || product.getCategory() == null)
            return false;
        String productCategory = product.getCategory().getName().trim();
        return promo.getTargetCategories().stream()
                .anyMatch(target -> target.trim().equalsIgnoreCase(productCategory));
    }

    private boolean matchesCollection(PromotionEntity promo, ProductEntity product) {
        if (isEmpty(promo.getTargetCollections()) || product.getCollection() == null)
            return false;
        String productCollection = product.getCollection().getName().trim();
        return promo.getTargetCollections().stream()
                .anyMatch(target -> target.trim().equalsIgnoreCase(productCollection));
    }

    private boolean matchesTag(PromotionEntity promo, ProductEntity product) {
        if (isEmpty(promo.getTargetTags()) || product.getTags() == null || product.getTags().isEmpty())
            return false;

        return product.getTags().stream()
                .map(String::trim)
                .anyMatch(pTag -> promo.getTargetTags().stream()
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

    private boolean isEmpty(Set<?> set) {
        return set == null || set.isEmpty();
    }
}

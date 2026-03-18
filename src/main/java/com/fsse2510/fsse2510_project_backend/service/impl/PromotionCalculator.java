package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.promotion.cache.CachedPromotion;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
import com.fsse2510.fsse2510_project_backend.data.promotion.promotionType.PromotionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.fsse2510.fsse2510_project_backend.util.BusinessConstants.MONEY_ROUNDING;

@Service
public class PromotionCalculator {

    // ========== CachedPromotion overloads (used by enrichers) ==========

    public BigDecimal calculateDiscountAmount(CachedPromotion promo, BigDecimal originalPrice, Integer quantity) {
        if (promo == null) {
            return BigDecimal.ZERO;
        }
        return calculateDiscountAmount(
                promo.type(), promo.discountType(), promo.discountValue(),
                promo.buyX(), promo.getY(), originalPrice, quantity);
    }

    public BigDecimal calculatePromotionalPrice(BigDecimal originalPrice, CachedPromotion promo, Integer quantity) {
        if (promo == null) {
            return originalPrice.setScale(2, MONEY_ROUNDING);
        }
        return calculatePromotionalPrice(
                originalPrice, promo.type(), promo.discountType(), promo.discountValue(),
                promo.buyX(), promo.getY(), quantity);
    }

    public String generateBadgeText(CachedPromotion promo) {
        if (promo == null) {
            return null;
        }
        return generateBadgeText(
                promo.type(), promo.discountType(), promo.discountValue(),
                promo.buyX(), promo.getY(),
                promo.minQuantity(), promo.minAmount(), promo.targetMemberLevel());
    }

    // ========== PromotionEntity overloads (used by sync services within @Transactional) ==========

    public BigDecimal calculateDiscountAmount(PromotionEntity promo, BigDecimal originalPrice, Integer quantity) {
        if (promo == null) {
            return BigDecimal.ZERO;
        }
        return calculateDiscountAmount(
                promo.getType(), promo.getDiscountType(), promo.getDiscountValue(),
                promo.getBuyX(), promo.getGetY(), originalPrice, quantity);
    }

    public BigDecimal calculatePromotionalPrice(BigDecimal originalPrice, PromotionEntity promo, Integer quantity) {
        if (promo == null) {
            return originalPrice.setScale(2, MONEY_ROUNDING);
        }
        return calculatePromotionalPrice(
                originalPrice, promo.getType(), promo.getDiscountType(), promo.getDiscountValue(),
                promo.getBuyX(), promo.getGetY(), quantity);
    }

    public String generateBadgeText(PromotionEntity promotion) {
        if (promotion == null) {
            return null;
        }
        return generateBadgeText(
                promotion.getType(), promotion.getDiscountType(), promotion.getDiscountValue(),
                promotion.getBuyX(), promotion.getGetY(),
                promotion.getMinQuantity(), promotion.getMinAmount(), promotion.getTargetMemberLevel());
    }

    // ========== Shared implementation (type-safe, no JPA dependency) ==========

    private BigDecimal calculateDiscountAmount(
            PromotionType type, DiscountType discountType, BigDecimal discountValue,
            Integer buyX, Integer getY, BigDecimal originalPrice, Integer quantity) {

        if (type == PromotionType.BUY_X_GET_Y_FREE) {
            int bx = buyX != null ? buyX : 0;
            int gy = getY != null ? getY : 0;
            int cycle = bx + gy;

            if (cycle > 0) {
                if (quantity != null && quantity > 0) {
                    int freeUnitsCount = (quantity / cycle) * gy;
                    BigDecimal totalFreeAmount = originalPrice.multiply(BigDecimal.valueOf(freeUnitsCount));
                    return totalFreeAmount.divide(BigDecimal.valueOf(quantity), 10, MONEY_ROUNDING);
                } else {
                    BigDecimal freeUnits = BigDecimal.valueOf(gy);
                    return originalPrice.multiply(freeUnits)
                            .divide(BigDecimal.valueOf(cycle), 10, MONEY_ROUNDING);
                }
            }
            return BigDecimal.ZERO;
        }

        if (discountType == null || discountValue == null) {
            return BigDecimal.ZERO;
        }

        if (discountType == DiscountType.PERCENTAGE) {
            return originalPrice.multiply(
                    discountValue.divide(BigDecimal.valueOf(100), 10, MONEY_ROUNDING));
        } else if (discountType == DiscountType.FIXED) {
            return discountValue;
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculatePromotionalPrice(
            BigDecimal originalPrice, PromotionType type, DiscountType discountType,
            BigDecimal discountValue, Integer buyX, Integer getY, Integer quantity) {

        if (discountType == null || discountValue == null) {
            if (type == PromotionType.BUY_X_GET_Y_FREE) {
                int bx = buyX != null ? buyX : 0;
                int gy = getY != null ? getY : 0;
                int cycle = bx + gy;
                if (cycle > 0) {
                    if (quantity != null && quantity > 0) {
                        int freeUnitsCount = (quantity / cycle) * gy;
                        BigDecimal totalOriginal = originalPrice.multiply(BigDecimal.valueOf(quantity));
                        BigDecimal totalFreeAmount = originalPrice.multiply(BigDecimal.valueOf(freeUnitsCount));
                        return totalOriginal.subtract(totalFreeAmount)
                                .divide(BigDecimal.valueOf(quantity), 10, MONEY_ROUNDING)
                                .setScale(2, MONEY_ROUNDING);
                    } else {
                        return originalPrice.multiply(BigDecimal.valueOf(bx))
                                .divide(BigDecimal.valueOf(cycle), 10, MONEY_ROUNDING)
                                .setScale(2, MONEY_ROUNDING);
                    }
                }
            }
            return originalPrice.setScale(2, MONEY_ROUNDING);
        }

        return switch (discountType) {
            case PERCENTAGE -> {
                BigDecimal discountFactor = BigDecimal.valueOf(100).subtract(discountValue)
                        .divide(BigDecimal.valueOf(100), 10, MONEY_ROUNDING);
                yield originalPrice.multiply(discountFactor).setScale(2, MONEY_ROUNDING);
            }
            case FIXED -> originalPrice.subtract(discountValue).max(BigDecimal.ZERO).setScale(2,
                    MONEY_ROUNDING);
        };
    }

    private String generateBadgeText(
            PromotionType type, DiscountType discountType, BigDecimal discountValue,
            Integer buyX, Integer getY, Integer minQuantity, BigDecimal minAmount,
            com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel targetMemberLevel) {

        if (type == PromotionType.BUY_X_GET_Y_FREE) {
            int bx = buyX != null ? buyX : 0;
            int gy = getY != null ? getY : 0;
            return String.format("Buy %d Get %d Free", bx, gy);
        }

        String discountStr = "";
        if (discountType == DiscountType.PERCENTAGE && discountValue != null) {
            discountStr = String.format("%s%% OFF", discountValue.stripTrailingZeros().toPlainString());
        } else if (discountType == DiscountType.FIXED && discountValue != null) {
            discountStr = String.format("$%s OFF", discountValue.stripTrailingZeros().toPlainString());
        }

        if (type == PromotionType.MIN_QUANTITY_DISCOUNT) {
            int minQty = minQuantity != null ? minQuantity : 0;
            if (!discountStr.isEmpty()) {
                return String.format("Buy %d+ Get %s", minQty, discountStr);
            }
        }

        if (type == PromotionType.MIN_AMOUNT_DISCOUNT) {
            if (minAmount != null && !discountStr.isEmpty()) {
                return String.format("Spend $%s+ Get %s",
                        minAmount.stripTrailingZeros().toPlainString(), discountStr);
            }
        }

        if (type == PromotionType.BUNDLE_DISCOUNT) {
            if (minQuantity != null && minQuantity > 1 && !discountStr.isEmpty()) {
                return String.format("Buy %d Get %s", minQuantity, discountStr);
            }
        }

        if (type == PromotionType.MEMBERSHIP_DISCOUNT) {
            String level = targetMemberLevel != null
                    ? targetMemberLevel.name()
                    : "MEMBER";
            if (!discountStr.isEmpty()) {
                return String.format("%s+ EXCLUSIVE %s", level, discountStr);
            }
            return String.format("%s+ EXCLUSIVE", level);
        }

        if (!discountStr.isEmpty()) {
            return discountStr;
        }
        return "SALE";
    }
}
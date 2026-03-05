package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
import com.fsse2510.fsse2510_project_backend.data.promotion.promotionType.PromotionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PromotionCalculator {

    public BigDecimal calculateDiscountAmount(PromotionEntity promo, BigDecimal originalPrice) {
        if (promo == null) {
            return BigDecimal.ZERO;
        }

        if (promo.getType() == PromotionType.BUY_X_GET_Y_FREE) {
            // Estimate: y free unit(s) per cycle — used only for best-promo selection ranking
            // For example, B6G2 -> original/3
            int buyX = promo.getBuyX() != null ? promo.getBuyX() : 0;
            int getY = promo.getGetY() != null ? promo.getGetY() : 0;
            int cycle = buyX + getY;

            if (cycle > 0) {
                // (originalPrice * getY) / cycle
                BigDecimal freeUnits = BigDecimal.valueOf(getY);
                return originalPrice.multiply(freeUnits)
                        .divide(BigDecimal.valueOf(cycle), 4, RoundingMode.HALF_UP);
            }
            return BigDecimal.ZERO;
        }

        if (promo.getDiscountType() == null || promo.getDiscountValue() == null) {
            return BigDecimal.ZERO;
        }

        if (promo.getDiscountType() == DiscountType.PERCENTAGE) {
            // discountValue = 20 means 20% discount amount
            return originalPrice.multiply(
                    promo.getDiscountValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        } else if (promo.getDiscountType() == DiscountType.FIXED) {
            return promo.getDiscountValue();
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calculatePromotionalPrice(BigDecimal originalPrice, PromotionEntity promo) {
        if (promo.getDiscountType() == null || promo.getDiscountValue() == null) {
            return originalPrice;
        }

        return switch (promo.getDiscountType()) {
            case PERCENTAGE -> {
                BigDecimal discountFactor = BigDecimal.valueOf(100).subtract(promo.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                yield originalPrice.multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);
            }
            case FIXED -> originalPrice.subtract(promo.getDiscountValue()).max(BigDecimal.ZERO).setScale(2,
                    RoundingMode.HALF_UP);
        };
    }

    //UI Badge
    public String generateBadgeText(PromotionEntity promotion) {
        if (promotion == null) {
            return null;
        }

        if (promotion.getType() == PromotionType.BUY_X_GET_Y_FREE) {
            int buyX = promotion.getBuyX() != null ? promotion.getBuyX() : 0;
            int getY = promotion.getGetY() != null ? promotion.getGetY() : 0;
            return String.format("Buy %d Get %d Free", buyX, getY);
        }

        String discountStr = "";
        if (promotion.getDiscountType() == DiscountType.PERCENTAGE && promotion.getDiscountValue() != null) {
            discountStr = String.format("%d%% OFF", promotion.getDiscountValue().intValue());
        } else if (promotion.getDiscountType() == DiscountType.FIXED && promotion.getDiscountValue() != null) {
            discountStr = String.format("$%s OFF", promotion.getDiscountValue().stripTrailingZeros().toPlainString());
        }

        if (promotion.getType() == PromotionType.MIN_QUANTITY_DISCOUNT) {
            int minQty = promotion.getMinQuantity() != null ? promotion.getMinQuantity() : 0;
            if (!discountStr.isEmpty()) {
                return String.format("Buy %d+ Get %s", minQty, discountStr);
            }
        }

        if (promotion.getType() == PromotionType.MIN_AMOUNT_DISCOUNT) {
            if (promotion.getMinAmount() != null && !discountStr.isEmpty()) {
                return String.format("Spend $%s+ Get %s",
                        promotion.getMinAmount().stripTrailingZeros().toPlainString(), discountStr);
            }
        }

        if (promotion.getType() == PromotionType.BUNDLE_DISCOUNT) {
            if (promotion.getMinQuantity() != null && promotion.getMinQuantity() > 1 && !discountStr.isEmpty()) {
                return String.format("Buy %d Get %s", promotion.getMinQuantity(), discountStr);
            }
        }

        if (promotion.getType() == PromotionType.MEMBERSHIP_DISCOUNT) {
            String level = promotion.getTargetMemberLevel() != null
                    ? promotion.getTargetMemberLevel().name()
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
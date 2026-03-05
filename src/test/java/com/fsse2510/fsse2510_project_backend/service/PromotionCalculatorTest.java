package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
import com.fsse2510.fsse2510_project_backend.data.promotion.promotionType.PromotionType;
import com.fsse2510.fsse2510_project_backend.service.impl.PromotionCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PromotionCalculatorTest {

        @InjectMocks
        private PromotionCalculator calculator;

        @Test
        void testCalculateDiscountAmount_Percentage() {
                PromotionEntity promo = new PromotionEntity();
                promo.setDiscountType(DiscountType.PERCENTAGE);
                promo.setDiscountValue(new BigDecimal("20.00")); // 20% off

                BigDecimal originalPrice = new BigDecimal("100.00");
                BigDecimal discount = calculator.calculateDiscountAmount(promo, originalPrice);

                // 20% of 100 = 20
                // Use compareTo to ignore scale differences (e.g. 20.0000 vs 20.000000)
                assertEquals(0, new BigDecimal("20.0000").compareTo(discount));
        }

        @Test
        void testCalculateDiscountAmount_Fixed() {
                PromotionEntity promo = new PromotionEntity();
                promo.setDiscountType(DiscountType.FIXED);
                promo.setDiscountValue(new BigDecimal("15.00")); // $15 off

                BigDecimal originalPrice = new BigDecimal("100.00");
                BigDecimal discount = calculator.calculateDiscountAmount(promo, originalPrice);

                assertEquals(new BigDecimal("15.00"), discount);
        }

        @Test
        void testCalculatePromotionalPrice_Percentage() {
                PromotionEntity promo = new PromotionEntity();
                promo.setDiscountType(DiscountType.PERCENTAGE);
                promo.setDiscountValue(new BigDecimal("25.00")); // 25% off

                BigDecimal originalPrice = new BigDecimal("200.00");
                BigDecimal price = calculator.calculatePromotionalPrice(originalPrice, promo);

                // 200 * (1 - 0.25) = 150
                assertEquals(new BigDecimal("150.00"), price);
        }

        @Test
        void testCalculatePromotionalPrice_Fixed() {
                PromotionEntity promo = new PromotionEntity();
                promo.setDiscountType(DiscountType.FIXED);
                promo.setDiscountValue(new BigDecimal("50.00")); // $50 off

                BigDecimal originalPrice = new BigDecimal("200.00");
                BigDecimal price = calculator.calculatePromotionalPrice(originalPrice, promo);

                assertEquals(new BigDecimal("150.00"), price);
        }

        @Test
        void testCalculatePromotionalPrice_Fixed_MoreThanPrice() {
                PromotionEntity promo = new PromotionEntity();
                promo.setDiscountType(DiscountType.FIXED);
                promo.setDiscountValue(new BigDecimal("300.00")); // $300 off

                BigDecimal originalPrice = new BigDecimal("200.00");
                BigDecimal price = calculator.calculatePromotionalPrice(originalPrice, promo);

                // Should be 0, not negative
                assertEquals(0, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP).compareTo(price));
        }

        @Test
        void testGenerateBadgeText_Percentage() {
                PromotionEntity promo = new PromotionEntity();
                promo.setType(PromotionType.STOREWIDE_SALE);
                promo.setDiscountType(DiscountType.PERCENTAGE);
                promo.setDiscountValue(new BigDecimal("15.00"));

                assertEquals("15% OFF", calculator.generateBadgeText(promo));
        }

        @Test
        void testGenerateBadgeText_Fixed() {
                PromotionEntity promo = new PromotionEntity();
                promo.setType(PromotionType.STOREWIDE_SALE);
                promo.setDiscountType(DiscountType.FIXED);
                promo.setDiscountValue(new BigDecimal("10.50"));

                assertEquals("$10.5 OFF", calculator.generateBadgeText(promo));
        }

        @Test
        void testGenerateBadgeText_B2GY() {
                PromotionEntity promo = new PromotionEntity();
                promo.setType(PromotionType.BUY_X_GET_Y_FREE);
                promo.setBuyX(2);
                promo.setGetY(1);

                assertEquals("Buy 2 Get 1 Free", calculator.generateBadgeText(promo));
        }

        @Test
        void testGenerateBadgeText_MembershipExclusive() {
                PromotionEntity promo = new PromotionEntity();
                promo.setType(PromotionType.MEMBERSHIP_DISCOUNT);
                promo.setTargetMemberLevel(MembershipLevel.GOLD);
                promo.setDiscountType(DiscountType.PERCENTAGE);
                promo.setDiscountValue(new BigDecimal("20.00"));

                assertEquals("GOLD+ EXCLUSIVE 20% OFF", calculator.generateBadgeText(promo));
        }
}

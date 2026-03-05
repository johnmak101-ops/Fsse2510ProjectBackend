package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;

public interface PromotionApplicabilityService {

    boolean isApplicable(PromotionEntity promo, ProductEntity product, boolean isStrict);

    // New: Check product targeting only (ignoring user/cart conditions)
    // Used for potential badge display on public pages
    boolean isProductEligibleForPromotion(PromotionEntity promo, ProductEntity product);

    boolean isApplicable(PromotionEntity promo, ProductEntity product,
                         UserEntity user, boolean isStrict);
}

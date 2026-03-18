package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import com.fsse2510.fsse2510_project_backend.data.promotion.cache.CachedPromotion;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;

public interface PromotionApplicabilityService {

    // ========== PromotionEntity overloads (sync services within @Transactional) ==========

    boolean isApplicable(PromotionEntity promo, ProductEntity product, boolean isStrict);

    boolean isProductEligibleForPromotion(PromotionEntity promo, ProductEntity product);

    boolean isApplicable(PromotionEntity promo, ProductEntity product,
                         UserEntity user, boolean isStrict);

    // ========== CachedPromotion overloads (enrichers using cached data) ==========

    boolean isApplicable(CachedPromotion promo, ProductEntity product, boolean isStrict);

    boolean isProductEligibleForPromotion(CachedPromotion promo, ProductEntity product);

    boolean isApplicable(CachedPromotion promo, ProductEntity product,
                         UserEntity user, boolean isStrict);
}

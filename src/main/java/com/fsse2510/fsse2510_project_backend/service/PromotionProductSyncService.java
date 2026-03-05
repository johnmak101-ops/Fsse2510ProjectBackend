package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;

public interface PromotionProductSyncService {

    void applyPromotionToProductsAsync(PromotionEntity promotion);

    void removePromotionFromProductsAsync(Integer promotionId);

    void removePromotionFromProductsSync(Integer promotionId);

    void clearExpiredPromotions();

    void clearProductCache();
}

package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.promotion.domainObject.request.CreatePromotionRequestData;
import com.fsse2510.fsse2510_project_backend.data.promotion.domainObject.request.UpdatePromotionRequestData;
import com.fsse2510.fsse2510_project_backend.data.promotion.domainObject.response.PromotionResponseData;
import com.fsse2510.fsse2510_project_backend.data.promotion.promotionType.PromotionType;

import java.util.List;

public interface PromotionService {
    PromotionResponseData createPromotion(CreatePromotionRequestData requestData);

    void assignPromotionToProduct(Integer promoId, Integer pid);

    List<PromotionResponseData> getActivePromotionsByType(List<PromotionType> types);

    List<PromotionResponseData> getAllPromotions();

    PromotionResponseData getPromotionById(Integer id);

    PromotionResponseData updatePromotion(Integer id, UpdatePromotionRequestData requestData);

    void deletePromotion(Integer id);

    // Public-facing: date-filtered by type
    List<PromotionResponseData> getActivePublicPromotions(List<PromotionType> types);
}
package com.fsse2510.fsse2510_project_backend.data.product.domainObject.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Marker interface for product DTOs that support promotion enrichment.
 * Allows generic methods to work with both ProductResponseData and
 * ProductSummaryData.
 */
public interface PromotionEnrichable {
    Integer getPid();

    BigDecimal getPrice();

    Boolean getIsSale();

    void setPrice(BigDecimal price);

    void setOriginalPrice(BigDecimal originalPrice);

    void setDiscountAmount(BigDecimal discountAmount);

    void setDiscountPercentage(BigDecimal discountPercentage);

    List<String> getPromotionBadgeTexts();

    void setPromotionBadgeTexts(List<String> badgeTexts);

    void setIsSale(Boolean isSale);
}

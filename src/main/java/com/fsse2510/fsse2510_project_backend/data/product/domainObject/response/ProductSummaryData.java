package com.fsse2510.fsse2510_project_backend.data.product.domainObject.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryData implements Serializable, PromotionEnrichable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer pid;
    private String name;
    private String slug;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private String category;
    private String productType;
    private Boolean isNew;
    private Boolean isSale;
    @Builder.Default
    private List<String> promotionBadgeTexts = new ArrayList<>();
    private Boolean isFeatured; // Added
    private Boolean hasStock; // Added
    private Integer stock; // Added: Total stock quantity
    private List<ProductImageSummaryData> images; // Added for color switching

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImageSummaryData implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private String url;
        private String tag;
    }
}

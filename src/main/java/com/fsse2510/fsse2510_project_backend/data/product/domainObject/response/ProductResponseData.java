package com.fsse2510.fsse2510_project_backend.data.product.domainObject.response;

import com.fsse2510.fsse2510_project_backend.data.promotion.domainObject.response.PromotionResponseData;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class ProductResponseData implements Serializable, PromotionEnrichable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer pid;
    private String name;
    private String slug;
    private String status;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private Integer stock; // Calculated total stock
    private Boolean hasStock; // Added
    private String category;
    private String collection;
    private String story;
    private Boolean isNew;
    private Boolean isSale;
    private Boolean isFeatured;
    private Integer featuredPriority;
    private String productType;
    private String vendor;
    private Long shopifyId;
    private String productIntro;
    private String fabricInfo;
    private String designStyling;
    private String colorDisclaimer;
    private Set<String> tags;
    private List<ProductImageResponseData> images;
    private List<ProductInventoryResponseData> inventories;
    @Builder.Default
    private List<String> promotionBadgeTexts = new ArrayList<>();
    private PromotionResponseData promotion;

    // Support for Dynamic Metadata
    private com.fsse2510.fsse2510_project_backend.data.product.domainObject.ProductDetails details;
}

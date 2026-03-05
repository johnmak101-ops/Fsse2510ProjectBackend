package com.fsse2510.fsse2510_project_backend.data.product.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductSummaryResponseDto {
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

    @JsonProperty("isNew")
    private Boolean newlyAdded;

    @JsonProperty("isSale")
    private Boolean onSale;

    private Integer appliedPromotionId;
    private String promotionBadgeText;

    @JsonProperty("isFeatured")
    private Boolean featured;

    private Boolean hasStock;
    private Integer stock;
}

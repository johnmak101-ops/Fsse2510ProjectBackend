package com.fsse2510.fsse2510_project_backend.data.product.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.ProductDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDetailResponseDto {
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
    private Integer stock; // Total stock
    private String category;
    private String collection;
    private String story;
    @JsonProperty("isNew")
    private Boolean newlyAdded;
    @JsonProperty("isSale")
    private Boolean onSale;
    @JsonProperty("isFeatured")
    private Boolean featured;

    private String productType;
    private String vendor;
    private Long shopifyId;
    private String productIntro;
    private String fabricInfo;
    private String designStyling;
    private String colorDisclaimer;
    private List<String> tags;
    private Boolean hasStock;
    private List<ProductImageResponseDto> images;
    private List<ProductInventoryResponseDto> inventories;

    // Support for Dynamic Metadata (New!)
    private ProductDetails details;
    private String promotionBadgeText;
}

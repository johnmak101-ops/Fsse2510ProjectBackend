package com.fsse2510.fsse2510_project_backend.data.cartitem.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDto {
    private Integer pid;
    private String slug;
    private String sku;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private java.util.List<String> promotionBadgeTexts;
    private Integer cartQuantity;
    private Integer stock;
    private String selectedSize;
    private String selectedColor;

    // Product metadata for frontend promotion eligibility checks
    private java.util.List<Integer> appliedPromotionIds;
    private String category;
    private String collection;
    private java.util.List<String> tags;
}

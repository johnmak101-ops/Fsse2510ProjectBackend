package com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.response;

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
public class CartItemResponseData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer pid;
    private String slug;
    private String sku;
    private String name;
    private String imageUrl;
    private BigDecimal price; // Final price after discount
    private BigDecimal originalPrice; // Original price before discount
    private BigDecimal discountAmount; // Discount amount (original - final)
    private BigDecimal discountPercentage; // Discount percentage (0.2 for 20%)
    @Builder.Default
    private List<String> promotionBadgeTexts = new ArrayList<>();
    private Integer cartQuantity;
    private Integer stock;
    private String selectedSize;
    private String selectedColor;

    // Product metadata for frontend promotion eligibility checks
    @Builder.Default
    private List<Integer> appliedPromotionIds = new ArrayList<>();
    private String category;
    private String collection;
    private Set<String> tags;
}

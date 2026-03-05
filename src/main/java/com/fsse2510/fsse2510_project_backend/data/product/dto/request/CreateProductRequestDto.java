package com.fsse2510.fsse2510_project_backend.data.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequestDto {

    @NotBlank(message = "Product name cannot be empty")
    private String name;

    @NotBlank(message = "Slug is required")
    private String slug;

    private String status;

    private String description;

    private String imageUrl;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;

    private String category;
    private String collection;
    private String story;
    private Boolean isNew;
    private Boolean isSale;
    private Long shopifyId;
    private String vendor;
    private String productType;
    private String mainCategory;
    private String productIntro;
    private String fabricInfo;
    private String designStyling;
    private String colorDisclaimer;
    private java.util.List<String> tags;
    private java.util.List<ProductImageRequestDto> images;

    private java.util.List<ProductInventoryRequestDto> inventories;
}

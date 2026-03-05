package com.fsse2510.fsse2510_project_backend.data.product.domainObject.request;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequestData {
    private Integer pid;
    private String name;
    private String slug;
    private String status;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private String category;
    private String collection;
    private String story;
    private Boolean isNew;
    private Boolean isSale;
    private Long shopifyId;
    private String vendor;
    private String productType;
    private String productIntro;
    private String fabricInfo;
    private String designStyling;
    private String colorDisclaimer;
    private List<String> tags;
    private List<ProductImageRequestData> images;
    private List<ProductInventoryRequestData> inventories;

    // Support for JSON mapping
    private com.fsse2510.fsse2510_project_backend.data.product.domainObject.ProductDetails details;
}

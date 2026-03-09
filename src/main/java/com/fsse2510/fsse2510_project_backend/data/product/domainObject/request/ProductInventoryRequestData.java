package com.fsse2510.fsse2510_project_backend.data.product.domainObject.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInventoryRequestData {
    private Integer id; // Added ID for updates
    private String sku;
    private String size;
    private String color;
    private Integer stock;
    private Integer stockReserved;
    private BigDecimal weight;
}

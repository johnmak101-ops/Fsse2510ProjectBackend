package com.fsse2510.fsse2510_project_backend.data.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInventoryResponseDto {
    private Integer id;
    private String sku;
    private String size;
    private String color;
    private Integer stock;
    private Integer stockReserved;
    private java.math.BigDecimal weight;
}

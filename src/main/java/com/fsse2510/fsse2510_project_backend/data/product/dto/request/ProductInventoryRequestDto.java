package com.fsse2510.fsse2510_project_backend.data.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInventoryRequestDto {
    private Integer id; // Added ID for updates

    @NotBlank(message = "SKU is required")
    private String sku;

    private String size;

    private String color;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private Integer stockReserved;

    private BigDecimal weight;
}

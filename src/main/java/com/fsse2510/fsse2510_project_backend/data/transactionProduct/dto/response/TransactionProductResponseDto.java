package com.fsse2510.fsse2510_project_backend.data.transactionProduct.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionProductResponseDto {
    private Integer tpid;
    private Integer pid;
    private String sku;
    private String size;
    private String color;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}

package com.fsse2510.fsse2510_project_backend.data.cartitem.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemBatchRequestDto {
    private String sku;
    private Integer quantity;
}

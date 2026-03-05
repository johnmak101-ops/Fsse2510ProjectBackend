package com.fsse2510.fsse2510_project_backend.data.transactionProduct.domainObject.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionProductResponseData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer tpid;
    private Integer pid;
    private String sku;
    private String size;
    private String color;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price; // Final price after discount
    private BigDecimal originalPrice; // Original price before discount
    private BigDecimal discountAmount; // Discount amount (original - final)
    private BigDecimal discountPercentage; // Discount percentage (0.2 for 20%)
    private Integer quantity;
    private BigDecimal subtotal;
}

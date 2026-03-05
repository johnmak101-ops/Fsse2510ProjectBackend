package com.fsse2510.fsse2510_project_backend.data.product.domainObject.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchCriteria {
    private String collection;
    private List<String> categories;
    private String category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer lastPid;
    private String sortBy; // price_asc, price_desc, newest
    private String productType;
    private Boolean isNew;
    private String searchText;
}

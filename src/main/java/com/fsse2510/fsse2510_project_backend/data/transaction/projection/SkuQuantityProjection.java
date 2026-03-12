package com.fsse2510.fsse2510_project_backend.data.transaction.projection;

/**
 * Interface-based projection for aggregate SKU-quantity queries.
 * Spring Data JPA auto-maps column aliases to getter methods.
 */
public interface SkuQuantityProjection {
    String getSku();
    Long getTotalQuantity();
}

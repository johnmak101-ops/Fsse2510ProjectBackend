package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.product.domainObject.ProductDetails;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.CreateProductRequestData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.UpdateProductRequestData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductResponseData;

/**
 * Service interface for admin product management operations (CRUD).
 * Separated from ProductService to follow single-responsibility principle.
 */
public interface ProductAdminService {
    ProductResponseData createProduct(CreateProductRequestData createData);

    ProductResponseData updateProduct(UpdateProductRequestData updateData);

    ProductResponseData deleteProduct(Integer pid);

    ProductResponseData updateProductMetadata(Integer pid, ProductDetails details);

    void deductStock(String sku, Integer quantity);
}

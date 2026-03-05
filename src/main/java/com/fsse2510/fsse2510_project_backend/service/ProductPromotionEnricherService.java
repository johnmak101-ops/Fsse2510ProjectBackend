package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductResponseData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductSummaryData;

import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import java.util.List;

public interface ProductPromotionEnricherService {

    List<ProductResponseData> enrichWithPromotions(List<ProductResponseData> products);

    List<ProductResponseData> enrichWithPromotions(List<ProductResponseData> products, List<ProductEntity> entities);

    List<ProductSummaryData> enrichSummariesWithPromotions(List<ProductSummaryData> summaries);

    List<ProductSummaryData> enrichSummariesWithPromotions(List<ProductSummaryData> summaries,
            List<ProductEntity> entities);

    ProductResponseData enrichWithPromotions(ProductResponseData product);

    ProductResponseData enrichWithPromotions(ProductResponseData product, ProductEntity entity);

    void clearCache();
}

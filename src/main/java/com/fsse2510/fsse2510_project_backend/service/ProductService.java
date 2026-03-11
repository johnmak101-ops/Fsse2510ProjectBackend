package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.common.dto.response.SliceResponseDto;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.ProductSearchCriteria;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductAttributesData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductResponseData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductSummaryData;
import com.fsse2510.fsse2510_project_backend.data.showcase.domainObject.response.ShowcaseCollectionData;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for read-only product query operations.
 * Admin/CRUD operations have been moved to ProductAdminService.
 */
public interface ProductService {
        SliceResponseDto<ProductSummaryData> getAllProducts(int page, int size);

        ProductResponseData getProductById(Integer pid);

        ProductResponseData getProductBySlug(String slug);

        SliceResponseDto<ProductSummaryData> getRelatedProducts(String category, Integer currentPid, int limit);

        SliceResponseDto<ProductSummaryData> getShowcaseProducts(int limit);

        SliceResponseDto<ProductSummaryData> getYouMayAlsoLike(String collection, Integer currentPid, int limit);

    SliceResponseDto<ProductSummaryData> searchProducts(ProductSearchCriteria criteria, Pageable pageable);

    SliceResponseDto<ProductSummaryData> searchProducts(ProductSearchCriteria criteria, int page, int limit);

    ProductAttributesData getAttributes();

        List<ShowcaseCollectionData> getShowcaseCollections();
}

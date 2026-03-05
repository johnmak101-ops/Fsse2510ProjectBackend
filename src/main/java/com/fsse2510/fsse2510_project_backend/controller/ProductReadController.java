package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.common.dto.response.SliceResponseDto;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.ProductSearchCriteria;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductAttributesData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductSummaryData;
import com.fsse2510.fsse2510_project_backend.data.product.dto.response.ProductDetailResponseDto;
import com.fsse2510.fsse2510_project_backend.data.product.dto.response.ProductSummaryResponseDto;
import com.fsse2510.fsse2510_project_backend.data.showcase.dto.ShowcaseCollectionResponseDto;
import com.fsse2510.fsse2510_project_backend.mapper.product.ProductDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for handling public product-related operations.
 * <p>
 * This includes fetching product lists, details, search, and recommendations.
 * It provides endpoints for the storefront to display products.
 * </p>
 */
@RestController
@RequestMapping("/public/products")
@RequiredArgsConstructor
public class ProductReadController {

        private final ProductService productService;
        private final ProductDtoMapper productDtoMapper;

        /**
         * Retrieves a paginated list of all products (summary view).
         *
         * @param page Page number (0-indexed).
         * @param size Number of items per page.
         * @return A slice of product summaries.
         */
        @GetMapping
        public SliceResponseDto<ProductSummaryResponseDto> getAllProducts(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                SliceResponseDto<ProductSummaryData> slice = productService.getAllProducts(page, size);
                return SliceResponseDto.of(
                                slice.getContent().stream()
                                                .map(productDtoMapper::toSummaryResponseDto)
                                                .toList(),
                                slice.isHasNext());
        }

        /**
         * Retrieves detailed information for a specific product by ID.
         *
         * @param id The product ID.
         * @return The product detail DTO.
         */
        @GetMapping("/{id}")
        public ProductDetailResponseDto getProductById(@PathVariable Integer id) {
                return productDtoMapper.toDetailResponseDto(
                                productService.getProductById(id));
        }

        /**
         * Retrieves detailed information for a specific product by its slug.
         * Useful for SEO-friendly URLs.
         *
         * @param slug The product slug string.
         * @return The product detail DTO.
         */
        @GetMapping("/slug/{slug}")
        public ProductDetailResponseDto getProductBySlug(@PathVariable String slug) {
                return productDtoMapper.toDetailResponseDto(
                                productService.getProductBySlug(slug));
        }

        /**
         * Fetches recommended products based on category and current product context.
         *
         * @param category   The category to base recommendations on.
         * @param currentPid The ID of the currently viewed product (to exclude it).
         * @param limit      Max number of recommendations to return.
         * @return A slice of recommended product summaries.
         */
        @GetMapping("/recommendations")
        public SliceResponseDto<ProductSummaryResponseDto> getRecommendations(
                        @RequestParam String category,
                        @RequestParam Integer currentPid,
                        @RequestParam(defaultValue = "12") int limit) {
                SliceResponseDto<ProductSummaryData> slice = productService
                                .getRelatedProducts(category, currentPid, limit);
                return SliceResponseDto.of(
                                slice.getContent().stream()
                                                .map(productDtoMapper::toSummaryResponseDto)
                                                .toList(),
                                slice.isHasNext());
        }

        /**
         * Retrieves a list of products highlighted for the showcase/home page.
         *
         * @param limit Max number of products to return.
         * @return A slice of showcase product summaries.
         */
        @GetMapping("/showcase")
        public SliceResponseDto<ProductSummaryResponseDto> getShowcase(
                        @RequestParam(defaultValue = "12") int limit) {
                SliceResponseDto<ProductSummaryData> slice = productService
                                .getShowcaseProducts(limit);
                return SliceResponseDto.of(
                                slice.getContent().stream()
                                                .map(productDtoMapper::toSummaryResponseDto)
                                                .toList(),
                                slice.isHasNext());
        }

        /**
         * Retrieves "You May Also Like" products, typically from the same collection.
         *
         * @param collection The collection name.
         * @param currentPid The ID of the currently viewed product.
         * @param limit      Max number of items to return.
         * @return A slice of related product summaries.
         */
        @GetMapping("/you-may-also-like")
        public SliceResponseDto<ProductSummaryResponseDto> getYouMayAlsoLike(
                        @RequestParam String collection,
                        @RequestParam Integer currentPid,
                        @RequestParam(defaultValue = "4") int limit) {
                SliceResponseDto<ProductSummaryData> slice = productService.getYouMayAlsoLike(collection, currentPid,
                                limit);
                return SliceResponseDto.of(
                                slice.getContent().stream()
                                                .map(productDtoMapper::toSummaryResponseDto)
                                                .toList(),
                                slice.isHasNext());
        }

        /**
         * Retrieves available showcase collections (e.g., for banner navigation).
         *
         * @return A list of showcase collection DTOs.
         */
        @GetMapping("/showcase/collections")
        public List<ShowcaseCollectionResponseDto> getShowcaseCollections() {
                return productService.getShowcaseCollections().stream()
                                .map(productDtoMapper::toShowcaseDto)
                                .toList();
        }

        /**
         * Searches for products using various filters like collection, category, price
         * range, and text.
         * Supports pagination and sorting.
         *
         * @param collection  Filter by collection name.
         * @param categories  Filter by list of categories.
         * @param category    Filter by single category.
         * @param minPrice    Minimum price filter.
         * @param maxPrice    Maximum price filter.
         * @param sortBy      Sort criteria (e.g., 'newest', 'price_asc').
         * @param page        Page number for pagination.
         * @param limit       Items per page.
         * @param lastPid     Cursor for pagination (optional).
         * @param productType Filter by product type.
         * @param isNew       Filter by 'new' status.
         * @param searchText  Free text search query.
         * @return A slice of matching product summaries.
         */
        @GetMapping("/search")
        public SliceResponseDto<ProductSummaryResponseDto> searchProducts(
                        @RequestParam(required = false) String collection,
                        @RequestParam(required = false) List<String> categories,
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false) BigDecimal minPrice,
                        @RequestParam(required = false) BigDecimal maxPrice,
                        @RequestParam(defaultValue = "newest") String sortBy,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int limit,
                        @RequestParam(required = false) Integer lastPid,
                        @RequestParam(required = false) String productType,
                        @RequestParam(required = false) Boolean isNew,
                        @RequestParam(required = false) String searchText) {

                Sort sort = Sort.by(Sort.Direction.DESC, "pid");
                if ("price_asc".equalsIgnoreCase(sortBy)) {
                        sort = Sort.by(Sort.Direction.ASC, "price");
                } else if ("price_desc".equalsIgnoreCase(sortBy)) {
                        sort = Sort.by(Sort.Direction.DESC, "price");
                }

                ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                                .collection(collection)
                                .categories(categories)
                                .category(category)
                                .minPrice(minPrice)
                                .maxPrice(maxPrice)
                                .sortBy(sortBy)
                                .lastPid(lastPid)
                                .productType(productType)
                                .isNew(isNew)
                                .searchText(searchText)
                                .build();

                // If lastPid is present, we are doing cursor pagination, so always start from
                // offset 0
                int queryPage = (lastPid != null) ? 0 : page;

                SliceResponseDto<ProductSummaryData> slice = productService.searchProducts(criteria,
                                PageRequest.of(queryPage, limit, sort));

                return SliceResponseDto.of(
                                slice.getContent().stream()
                                                .map(productDtoMapper::toSummaryResponseDto)
                                                .toList(),
                                slice.isHasNext());
        }

        /**
         * Retrieves global product attributes for filtering (e.g., min/max price, all
         * categories).
         * Useful for populating search filters in the UI.
         *
         * @return ProductAttributesData containing available filter options.
         */
        @GetMapping("/attributes")
        public ProductAttributesData getAttributes() {
                return productService.getAttributes();
        }
}

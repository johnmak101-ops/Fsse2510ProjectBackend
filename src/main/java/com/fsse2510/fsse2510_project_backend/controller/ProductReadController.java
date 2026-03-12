package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.common.dto.response.SliceResponseDto;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.ProductSearchCriteria;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductSummaryData;
import com.fsse2510.fsse2510_project_backend.data.product.dto.response.ProductAttributesResponseDto;
import com.fsse2510.fsse2510_project_backend.data.product.dto.response.ProductDetailResponseDto;
import com.fsse2510.fsse2510_project_backend.data.product.dto.response.ProductSummaryResponseDto;
import com.fsse2510.fsse2510_project_backend.data.showcase.dto.response.ShowcaseCollectionResponseDto;
import com.fsse2510.fsse2510_project_backend.mapper.product.ProductDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/public/products")
@RequiredArgsConstructor
public class ProductReadController {

        private final ProductService productService;
        private final ProductDtoMapper productDtoMapper;

        @GetMapping
        public SliceResponseDto<ProductSummaryResponseDto> getAllProducts(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                return toSliceResponseDto(productService.getAllProducts(page, size));
        }

        @GetMapping("/{id}")
        public ProductDetailResponseDto getProductById(@PathVariable Integer id) {
                return productDtoMapper.toDetailResponseDto(productService.getProductById(id));
        }

        @GetMapping("/slug/{slug}")
        public ProductDetailResponseDto getProductBySlug(@PathVariable String slug) {
                return productDtoMapper.toDetailResponseDto(productService.getProductBySlug(slug));
        }

        @GetMapping("/recommendations")
        public SliceResponseDto<ProductSummaryResponseDto> getRecommendations(
                        @RequestParam String category,
                        @RequestParam Integer currentPid,
                        @RequestParam(defaultValue = "12") int limit) {
                return toSliceResponseDto(productService.getRelatedProducts(category, currentPid, limit));
        }

        @GetMapping("/showcase")
        public SliceResponseDto<ProductSummaryResponseDto> getShowcase(
                        @RequestParam(defaultValue = "12") int limit) {
                return toSliceResponseDto(productService.getShowcaseProducts(limit));
        }

        @GetMapping("/you-may-also-like")
        public SliceResponseDto<ProductSummaryResponseDto> getYouMayAlsoLike(
                        @RequestParam String collection,
                        @RequestParam Integer currentPid,
                        @RequestParam(defaultValue = "4") int limit) {
                return toSliceResponseDto(productService.getYouMayAlsoLike(collection, currentPid, limit));
        }

        @GetMapping("/showcase/collections")
        public List<ShowcaseCollectionResponseDto> getShowcaseCollections() {
                return productService.getShowcaseCollections().stream()
                                .map(productDtoMapper::toShowcaseDto)
                                .toList();
        }

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

                return toSliceResponseDto(productService.searchProducts(criteria, page, limit));
        }

        @GetMapping("/attributes")
        public ProductAttributesResponseDto getAttributes() {
                return productDtoMapper.toAttributesResponseDto(productService.getAttributes());
        }

        // ── Helper ──────────────────────────────────────────────────────────────────
        // Service always returns non-null SliceResponseDto; null checks were redundant.
        private SliceResponseDto<ProductSummaryResponseDto> toSliceResponseDto(
                        SliceResponseDto<ProductSummaryData> slice) {
                return SliceResponseDto.of(
                                slice.getContent().stream()
                                                .map(productDtoMapper::toSummaryResponseDto)
                                                .toList(),
                                slice.isHasNext());
        }
}

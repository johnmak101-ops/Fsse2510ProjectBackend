package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.common.dto.response.SliceResponseDto;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.ProductSearchCriteria;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductAttributesData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductResponseData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductSummaryData;
import com.fsse2510.fsse2510_project_backend.data.product.entity.CategoryEntity;
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import com.fsse2510.fsse2510_project_backend.data.showcase.domainObject.ShowcaseCollectionData;
import com.fsse2510.fsse2510_project_backend.exception.product.ProductNotFoundException;
import com.fsse2510.fsse2510_project_backend.exception.product.ProductServiceException;
import com.fsse2510.fsse2510_project_backend.mapper.product.ProductDataMapper;
import com.fsse2510.fsse2510_project_backend.repository.CategoryRepository;
import com.fsse2510.fsse2510_project_backend.repository.ProductRepository;
import com.fsse2510.fsse2510_project_backend.repository.ShowcaseCollectionRepository;
import com.fsse2510.fsse2510_project_backend.repository.SystemConfigRepository;
import com.fsse2510.fsse2510_project_backend.service.ProductPromotionEnricherService;
import com.fsse2510.fsse2510_project_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private static final String CACHE_PRODUCT = "product_v4";
    private static final String CACHE_RECOMMENDATIONS = "product_recommendations_v4";
    private static final String CACHE_ATTRIBUTES = "product_attributes_v4";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ShowcaseCollectionRepository showcaseCollectionRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final ProductDataMapper productDataMapper;
    private final ProductPromotionEnricherService productPromotionEnricherService;
    private final ProductSpecificationBuilder specificationBuilder;

    @Override
    @Transactional(readOnly = true)
    public SliceResponseDto<ProductSummaryData> getAllProducts(int page, int size) {
        Slice<Integer> pidSlice = productRepository.findAllProductIds(PageRequest.of(page, size));
        List<ProductEntity> entities = fetchProductsByIdsShallow(pidSlice.getContent());
        List<ProductSummaryData> content = entities.stream().map(productDataMapper::toSummaryData).toList();
        content = productPromotionEnricherService.enrichSummariesWithPromotions(content, entities);
        return SliceResponseDto.of(content, pidSlice.hasNext());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_PRODUCT, key = "#pid", sync = true)
    public ProductResponseData getProductById(Integer pid) {
        ProductEntity entity = productRepository.findByPidWithAllDetails(pid)
                .orElseThrow(() -> {
                    logger.warn("Product not found: ID={}", pid);
                    return new ProductNotFoundException(pid);
                });
        return productPromotionEnricherService.enrichWithPromotions(productDataMapper.toResponseData(entity), entity);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_PRODUCT, key = "#slug", sync = true)
    public ProductResponseData getProductBySlug(String slug) {
        ProductEntity entity = productRepository.findBySlug(slug)
                .orElseThrow(() -> {
                    logger.warn("Product not found: Slug={}", slug);
                    return new ProductNotFoundException("Product not found: " + slug);
                });
        return productPromotionEnricherService.enrichWithPromotions(productDataMapper.toResponseData(entity), entity);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_RECOMMENDATIONS, key = "#category + '-' + #currentPid", sync = true)
    public SliceResponseDto<ProductSummaryData> getRelatedProducts(String category, Integer currentPid, int limit) {
        Slice<Integer> pidSlice = productRepository
                .findResultIdsByCategoryAndPidNot(category, currentPid, PageRequest.of(0, limit));
        List<ProductEntity> entities = fetchProductsByIdsShallow(pidSlice.getContent());
        List<ProductSummaryData> content = entities.stream().map(productDataMapper::toSummaryData).toList();
        return SliceResponseDto.of(
                productPromotionEnricherService.enrichSummariesWithPromotions(content, entities),
                pidSlice.hasNext());
    }

    @Override
    @Transactional(readOnly = true)
    public SliceResponseDto<ProductSummaryData> getShowcaseProducts(int limit) {
        Slice<Integer> pidSlice = productRepository.findShowcaseProductIds(PageRequest.of(0, limit));
        List<ProductEntity> entities = fetchProductsByIdsShallow(pidSlice.getContent());
        List<ProductSummaryData> content = entities.stream()
                .map(productDataMapper::toSummaryData).toList();
        return SliceResponseDto.of(
                productPromotionEnricherService.enrichSummariesWithPromotions(content, entities),
                pidSlice.hasNext());
    }

    @Override
    @Transactional(readOnly = true)
    public SliceResponseDto<ProductSummaryData> getYouMayAlsoLike(String collection, Integer currentPid, int limit) {
        List<Integer> pids = productRepository.findRandomIdsByCollectionAndPidNot(collection, currentPid, limit);
        List<ProductEntity> entities = fetchProductsByIdsShallow(pids);
        List<ProductSummaryData> content = entities.stream().map(productDataMapper::toSummaryData).toList();
        content = productPromotionEnricherService.enrichSummariesWithPromotions(content, entities);
        return SliceResponseDto.of(content, false);
    }

    @Override
    @Transactional(readOnly = true)
    public SliceResponseDto<ProductSummaryData> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        Specification<ProductEntity> spec = specificationBuilder.fromCriteria(criteria);
       Page<ProductEntity> pageResult = productRepository.findAll(spec, pageable);

        // ID-first pattern: extract IDs from the search result and batch load full
        // entities
        List<Integer> ids = pageResult.getContent().stream().map(ProductEntity::getPid).toList();
        List<ProductEntity> fullEntities = fetchProductsByIdsShallow(ids);

        List<ProductSummaryData> content = fullEntities.stream().map(productDataMapper::toSummaryData).toList();
        content = productPromotionEnricherService.enrichSummariesWithPromotions(content, fullEntities);
        return SliceResponseDto.of(content, pageResult.hasNext());
    }

    @Override
    @Cacheable(value = CACHE_ATTRIBUTES, sync = true)
    public ProductAttributesData getAttributes() {
        try {
            List<String> categories = categoryRepository.findAll().stream()
                    .map(CategoryEntity::getName).toList();
            List<String> sizes = productRepository.findAllDistinctSizes();
            List<String> colors = productRepository.findAllDistinctColors();
            List<String> types = productRepository.findAllDistinctProductTypes();

            List<String> featuredCollections = systemConfigRepository.findByConfigKey("navbar_featured_collections")
                    .map(config -> List.of(config.getConfigValue().split(",")))
                    .orElseGet(this::getDefaultFeaturedCollections);

            return ProductAttributesData.builder()
                    .categories(categories).sizes(sizes).colors(colors)
                    .productTypes(types).featuredCollections(featuredCollections)
                    .build();
        } catch (Exception e) {
            logger.error("Error fetching attributes", e);
            throw new ProductServiceException("Failed to load attributes", e);
        }
    }

    @Override
    public List<ShowcaseCollectionData> getShowcaseCollections() {
        return showcaseCollectionRepository.findAllByActiveTrueOrderByOrderIndexAsc().stream()
                .map(e -> ShowcaseCollectionData.builder()
                        .id(e.getId()).title(e.getTitle()).imageUrl(e.getImageUrl())
                        .bannerUrl(e.getBannerUrl()).tag(e.getTag())
                        .build())
                .toList();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    protected List<ProductEntity> fetchProductsByIdsShallow(List<Integer> pids) {
        if (pids.isEmpty())
            return List.of();
        List<ProductEntity> products = productRepository.findAllByPidIn(pids);
        Map<Integer, ProductEntity> entityMap = products.stream()
                .collect(Collectors.toMap(ProductEntity::getPid, Function.identity()));
        return pids.stream().map(entityMap::get).filter(Objects::nonNull).toList();
    }

    private List<String> getDefaultFeaturedCollections() {
        return List.of("HEART OF NORDIC", "HOLIDAY", "NEW ARRIVALS", "STEIFF",
                "AIRY MOCO", "COZY DINER DREAM", "COZY BEAR", "SMOOTHIE", "HOTEL GETAWAY", "PLUSH");
    }
}
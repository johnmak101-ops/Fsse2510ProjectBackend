package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.product.domainObject.ProductDetails;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.CreateProductRequestData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.ProductImageRequestData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.ProductInventoryRequestData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.UpdateProductRequestData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductResponseData;
import com.fsse2510.fsse2510_project_backend.data.product.entity.CategoryEntity;
import com.fsse2510.fsse2510_project_backend.data.product.entity.CollectionEntity;
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductImageEntity;
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductInventoryEntity;
import com.fsse2510.fsse2510_project_backend.exception.cartitem.NotEnoughStockException;
import com.fsse2510.fsse2510_project_backend.exception.product.ProductAlreadyExistedException;
import com.fsse2510.fsse2510_project_backend.exception.product.ProductNotFoundException;
import com.fsse2510.fsse2510_project_backend.mapper.product.ProductDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.product.ProductEntityMapper;
import com.fsse2510.fsse2510_project_backend.repository.CategoryRepository;
import com.fsse2510.fsse2510_project_backend.repository.CollectionRepository;
import com.fsse2510.fsse2510_project_backend.repository.ProductInventoryRepository;
import com.fsse2510.fsse2510_project_backend.repository.ProductRepository;
import com.fsse2510.fsse2510_project_backend.service.ProductAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductAdminServiceImpl implements ProductAdminService {

    private static final String CACHE_PRODUCT = "product_v5";
    private static final String CACHE_RECOMMENDATIONS = "product_recommendations_v5";
    private static final String CACHE_ATTRIBUTES = "product_attributes_v5";
    private static final String CACHE_SHOWCASE_PRODUCTS = "product_showcase_v2";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CollectionRepository collectionRepository;
    private final ProductInventoryRepository productInventoryRepository;
    private final ProductEntityMapper productEntityMapper;
    private final ProductDataMapper productDataMapper;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public ProductResponseData createProduct(CreateProductRequestData createData) {
        if (productRepository.findBySlug(createData.getSlug()).isPresent()) {
            throw new ProductAlreadyExistedException("Product with slug already exists: " + createData.getSlug());
        }
        ProductEntity entity = productEntityMapper.toEntity(createData);

        if (hasText(createData.getCategory())) {
            entity.setCategory(findOrCreateCategory(createData.getCategory()));
        }
        if (hasText(createData.getCollection())) {
            entity.setCollection(findOrCreateCollection(createData.getCollection()));
        }

        ProductEntity saved = productRepository.save(entity);
        evictProductCache(saved);
        return productDataMapper.toResponseData(saved);
    }

    @Override
    @Transactional
    public ProductResponseData updateProduct(UpdateProductRequestData updateData) {
        ProductEntity entity = productRepository.findById(updateData.getPid())
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + updateData.getPid()));

        productEntityMapper.updateEntity(updateData, entity);

        if (hasText(updateData.getCategory())) {
            entity.setCategory(findOrCreateCategory(updateData.getCategory()));
        } else {
            entity.setCategory(null);
        }
        if (hasText(updateData.getCollection())) {
            entity.setCollection(findOrCreateCollection(updateData.getCollection()));
        } else {
            entity.setCollection(null);
        }

        updateInventories(entity, updateData.getInventories());
        updateImages(entity, updateData.getImages());

        ProductEntity saved = productRepository.save(entity);
        evictProductCache(saved);
        return productDataMapper.toResponseData(saved);
    }

    @Override
    @Transactional
    public ProductResponseData deleteProduct(Integer pid) {
        ProductEntity entity = productRepository.findById(pid)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + pid));
        productRepository.delete(entity);
        evictProductCache(entity);
        return productDataMapper.toResponseData(entity);
    }

    @Override
    @Transactional
    public ProductResponseData updateProductMetadata(Integer pid, ProductDetails details) {
        ProductEntity entity = productRepository.findById(pid)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + pid));
        entity.setDetails(details);
        ProductEntity saved = productRepository.save(entity);
        evictProductCache(saved);
        return productDataMapper.toResponseData(saved);
    }

    @Override
    @Transactional
    public void deductStock(String sku, Integer quantity) {
        int updatedRows = productInventoryRepository.deductStock(sku, quantity);
        if (updatedRows == 0) {
            throw new NotEnoughStockException("SKU not found OR Insufficient stock for SKU: " + sku);
        }
        ProductInventoryEntity inventory = productInventoryRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("SKU not found: " + sku));
        evictProductCache(inventory.getProduct());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private CategoryEntity findOrCreateCategory(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.save(
                        CategoryEntity.builder()
                                .name(name)
                                .slug(generateSlug(name))
                                .build()));
    }

    private CollectionEntity findOrCreateCollection(String name) {
        return collectionRepository.findByName(name)
                .orElseGet(() -> collectionRepository.save(
                        CollectionEntity.builder()
                                .name(name)
                                .slug(generateSlug(name))
                                .build()));
    }

    private void updateInventories(ProductEntity entity, List<ProductInventoryRequestData> newInventories) {
        if (newInventories == null)
            return;

        Set<ProductInventoryEntity> currentSet = entity.getInventories();
        if (currentSet == null) {
            currentSet = new HashSet<>();
            entity.setInventories(currentSet);
        }
        final Set<ProductInventoryEntity> processSet = currentSet;
        Set<Integer> updatedIds = new HashSet<>();

        for (ProductInventoryRequestData data : newInventories) {
            Optional<ProductInventoryEntity> existingOp = Optional.empty();

            if (data.getId() != null) {
                existingOp = processSet.stream()
                        .filter(i -> i.getId() != null && i.getId().equals(data.getId()))
                        .findFirst();
            }
            if (existingOp.isEmpty() && data.getSku() != null) {
                existingOp = processSet.stream()
                        .filter(i -> data.getSku().equals(i.getSku()))
                        .findFirst();
            }

            if (existingOp.isPresent()) {
                ProductInventoryEntity existing = existingOp.get();
                existing.setSku(data.getSku());
                existing.setSize(data.getSize());
                existing.setColor(data.getColor());
                existing.setStock(data.getStock());
                existing.setStockReserved(data.getStockReserved());
                existing.setWeight(data.getWeight());
                if (existing.getId() != null) {
                    updatedIds.add(existing.getId());
                }
            } else {
                processSet.add(ProductInventoryEntity.builder()
                        .sku(data.getSku()).size(data.getSize()).color(data.getColor())
                        .stock(data.getStock()).stockReserved(data.getStockReserved())
                        .weight(data.getWeight()).product(entity)
                        .build());
            }
        }
        processSet.removeIf(i -> i.getId() != null && !updatedIds.contains(i.getId()));
    }

    private void updateImages(ProductEntity entity, List<ProductImageRequestData> newImages) {
        if (newImages == null)
            return;

        Set<ProductImageEntity> currentSet = entity.getImages();
        if (currentSet == null) {
            currentSet = new HashSet<>();
            entity.setImages(currentSet);
        }
        final Set<ProductImageEntity> processSet = currentSet;
        Set<Integer> updatedIds = new HashSet<>();

        for (ProductImageRequestData data : newImages) {
            if (data.getId() != null) {
                processSet.stream()
                        .filter(img -> img.getId() != null && img.getId().equals(data.getId()))
                        .findFirst()
                        .ifPresentOrElse(existing -> {
                            existing.setUrl(data.getUrl());
                            existing.setTag(data.getTag());
                            updatedIds.add(data.getId());
                        }, () -> processSet.add(ProductImageEntity.builder()
                                .url(data.getUrl()).tag(data.getTag()).product(entity).build()));
            } else {
                processSet.add(ProductImageEntity.builder()
                        .url(data.getUrl()).tag(data.getTag()).product(entity).build());
            }
        }
        processSet.removeIf(img -> img.getId() != null && !updatedIds.contains(img.getId()));
    }

    private void evictProductCache(ProductEntity product) {
        Cache cache = cacheManager.getCache(CACHE_PRODUCT);
        if (cache != null) {
            cache.evict(product.getPid());
            if (product.getSlug() != null) {
                cache.evict(product.getSlug());
            }
        }
        clearCollectionCaches();
    }

    private void clearCollectionCaches() {
        for (String name : List.of(CACHE_RECOMMENDATIONS, CACHE_ATTRIBUTES, CACHE_SHOWCASE_PRODUCTS)) {
            Cache c = cacheManager.getCache(name);
            if (c != null) {
                c.clear();
            }
        }
    }

    private boolean hasText(String str) {
        return str != null && !str.isBlank();
    }

    private String generateSlug(String name) {
        if (name == null)
            return "";
        // Convert to lowercase, replace any non-alphanumeric characters with a hyphen,
        // and optionally replace multiple hyphens with a single one.
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-+$", "").replaceAll("^-+", "");
    }
}

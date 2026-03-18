package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductResponseData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductSummaryData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.PromotionEnrichable;
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import com.fsse2510.fsse2510_project_backend.data.promotion.cache.CachedPromotion;
import com.fsse2510.fsse2510_project_backend.data.promotion.promotionType.PromotionType;
import com.fsse2510.fsse2510_project_backend.repository.ProductRepository;
import com.fsse2510.fsse2510_project_backend.repository.PromotionRepository;
import com.fsse2510.fsse2510_project_backend.service.ProductPromotionEnricherService;
import com.fsse2510.fsse2510_project_backend.service.PromotionApplicabilityService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fsse2510.fsse2510_project_backend.util.BusinessConstants.MONEY_ROUNDING;

/*
 * Service responsible for enriching product data (ProductResponseData,
 * ProductSummaryData)
 * with the best applicable promotions.
 *
 * 1. O(1) Memory Indexing over Database Queries: To prevent N+1 DB query
 * problems when fetching
 * hundreds of products, active promotions are loaded into memory and indexed
 * into HashMaps.
 * 2. Generics-based enrichment (`PromotionEnrichable`): Both
 * ProductResponseData and ProductSummaryData
 * share the identical enrichment logic. Using generics eliminates code
 * duplication and bugs.
 * 3. Double-Checked Locking Cache: Uses `volatile` for low-latency thread-safe
 * reads of the promotions
 * index, and synchronizes only when the cache has expired and needs refreshing
 * from the database.
 * 4. Cache POJO: Uses CachedPromotion (JPA-free record) instead of
 * PromotionEntity to avoid LazyInitializationException on detached entities.
 */
@Service
@RequiredArgsConstructor
public class ProductPromotionEnricherServiceImpl implements ProductPromotionEnricherService {

    private static final Logger logger = LoggerFactory.getLogger(ProductPromotionEnricherServiceImpl.class);

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final PromotionApplicabilityService promotionApplicabilityService;
    private final PromotionCalculator promotionCalculator;

    // Price-reducing types declared as constant for O(1) lookup
    private static final EnumSet<PromotionType> PRICE_REDUCING_TYPES = EnumSet.of(
            PromotionType.SPECIFIC_PRODUCT_DISCOUNT,
            PromotionType.SPECIFIC_CATEGORY_DISCOUNT,
            PromotionType.SPECIFIC_COLLECTION_DISCOUNT,
            PromotionType.SPECIFIC_TAG_DISCOUNT,
            PromotionType.STOREWIDE_SALE,
            PromotionType.MEMBERSHIP_DISCOUNT);

    // Optimized Cache: Using a structured index for fast lookup
    private volatile PromotionIndex promotionIndex;
    private volatile LocalDateTime cacheExpiry;

    /*
     * Inner class acting as an in-memory database index for Active Promotions.
     * Now uses CachedPromotion (JPA-free POJO) instead of PromotionEntity.
     */
    private static class PromotionIndex {
        final List<CachedPromotion> allPromos;
        final List<CachedPromotion> storewidePromos;
        final Map<Integer, List<CachedPromotion>> pidToPromos;
        final Map<String, List<CachedPromotion>> categoryToPromos;
        final Map<String, List<CachedPromotion>> collectionToPromos;
        final Map<String, List<CachedPromotion>> tagToPromos;

        PromotionIndex(List<CachedPromotion> promos) {
            this.allPromos = promos;
            this.storewidePromos = new ArrayList<>();
            this.pidToPromos = new HashMap<>();
            this.categoryToPromos = new HashMap<>();
            this.collectionToPromos = new HashMap<>();
            this.tagToPromos = new HashMap<>();

            List<CachedPromotion> sortedPromos = new ArrayList<>(promos);
            sortedPromos.sort((p1, p2) -> {
                BigDecimal v1 = p1.discountValue() != null ? p1.discountValue() : BigDecimal.ZERO;
                BigDecimal v2 = p2.discountValue() != null ? p2.discountValue() : BigDecimal.ZERO;
                return v2.compareTo(v1);
            });

            for (CachedPromotion p : sortedPromos) {
                if (p.type() == PromotionType.STOREWIDE_SALE || p.type() == PromotionType.MEMBERSHIP_DISCOUNT) {
                    storewidePromos.add(p);
                }
                if (!p.targetPids().isEmpty()) {
                    p.targetPids().forEach(pid -> pidToPromos.computeIfAbsent(pid, k -> new ArrayList<>()).add(p));
                }
                if (!p.targetCategories().isEmpty()) {
                    p.targetCategories().forEach(cat -> categoryToPromos
                            .computeIfAbsent(cat.trim().toLowerCase(), k -> new ArrayList<>()).add(p));
                }
                if (!p.targetCollections().isEmpty()) {
                    p.targetCollections().forEach(col -> collectionToPromos
                            .computeIfAbsent(col.trim().toLowerCase(), k -> new ArrayList<>()).add(p));
                }
                if (!p.targetTags().isEmpty()) {
                    p.targetTags().forEach(tag -> tagToPromos
                            .computeIfAbsent(tag.trim().toLowerCase(), k -> new ArrayList<>()).add(p));
                }
            }
        }

        boolean isEmpty() {
            return allPromos.isEmpty();
        }
    }

    // Public API
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseData> enrichWithPromotions(List<ProductResponseData> products) {
        if (products == null || products.isEmpty())
            return products;
        List<ProductEntity> entities = productRepository.findAllByPidIn(
                products.stream().map(ProductResponseData::getPid).toList());
        return enrichWithPromotions(products, entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseData> enrichWithPromotions(List<ProductResponseData> products,
            List<ProductEntity> entities) {
        return enrichList(products, entities, ProductResponseData::getPid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummaryData> enrichSummariesWithPromotions(List<ProductSummaryData> summaries) {
        if (summaries == null || summaries.isEmpty())
            return summaries;
        List<ProductEntity> entities = productRepository.findAllByPidIn(
                summaries.stream().map(ProductSummaryData::getPid).toList());
        return enrichSummariesWithPromotions(summaries, entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummaryData> enrichSummariesWithPromotions(List<ProductSummaryData> summaries,
            List<ProductEntity> entities) {
        return enrichList(summaries, entities, ProductSummaryData::getPid);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseData enrichWithPromotions(ProductResponseData product) {
        if (product == null)
            return null;
        ProductEntity entity = productRepository.findByPidWithAllDetails(product.getPid()).orElse(null);
        return enrichWithPromotions(product, entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseData enrichWithPromotions(ProductResponseData product, ProductEntity entity) {
        if (product == null)
            return null;
        PromotionIndex index = getCachedPromotionIndex();
        if (index.isEmpty())
            return setNoDiscount(product);
        return applyBestPromotion(product, index, entity);
    }

    @Override
    public void clearCache() {
        synchronized (this) {
            promotionIndex = null;
            cacheExpiry = null;
            logger.info("ProductPromotionEnricherService cache cleared manually.");
        }
    }

    // Generic Core Logic

    private <T extends PromotionEnrichable> List<T> enrichList(List<T> dtos,
            List<ProductEntity> entities,
            Function<T, Integer> pidExtractor) {
        if (dtos == null || dtos.isEmpty())
            return dtos;

        PromotionIndex index = getCachedPromotionIndex();
        if (index.isEmpty()) {
            return dtos.stream().map(this::setNoDiscount).toList();
        }

        Map<Integer, ProductEntity> entityMap = entities.stream()
                .collect(Collectors.toMap(ProductEntity::getPid, Function.identity()));

        return dtos.stream()
                .map(dto -> applyBestPromotion(dto, index, entityMap.get(pidExtractor.apply(dto))))
                .toList();
    }

    private <T extends PromotionEnrichable> T applyBestPromotion(T dto,
            PromotionIndex index,
            ProductEntity entity) {
        if (entity == null)
            return setNoDiscount(dto);

        dto.setPromotionBadgeTexts(new ArrayList<>());
        dto.setIsSale(false);

        Optional<CachedPromotion> best = findBestPromotion(entity, index);

        if (best.isEmpty()) {
            checkAndSetMembershipBadge(dto, index.allPromos, entity);
            return setNoDiscount(dto);
        }

        BigDecimal originalPrice = dto.getPrice();
        BigDecimal promoPrice = promotionCalculator.calculatePromotionalPrice(originalPrice, best.get(), null);
        BigDecimal discountAmount = originalPrice.subtract(promoPrice);
        BigDecimal discountPercentage = originalPrice.compareTo(BigDecimal.ZERO) > 0
                ? discountAmount.divide(originalPrice, 10, MONEY_ROUNDING)
                : BigDecimal.ZERO;

        dto.setOriginalPrice(originalPrice);

        if (PRICE_REDUCING_TYPES.contains(best.get().type())) {
            dto.setPrice(promoPrice);
            dto.setDiscountAmount(discountAmount);
            dto.setDiscountPercentage(discountPercentage);
        } else {
            dto.setPrice(originalPrice);
            dto.setDiscountAmount(BigDecimal.ZERO);
            dto.setDiscountPercentage(BigDecimal.ZERO);
        }

        String badge = promotionCalculator.generateBadgeText(best.get());
        if (badge != null) {
            dto.getPromotionBadgeTexts().add(badge);
        }
        dto.setIsSale(true);

        checkAndSetMembershipBadge(dto, index.allPromos, entity);

        return dto;
    }

    private <T extends PromotionEnrichable> void checkAndSetMembershipBadge(T dto,
            List<CachedPromotion> activePromos,
            ProductEntity entity) {
        activePromos.stream()
                .filter(p -> p.type() == PromotionType.MEMBERSHIP_DISCOUNT
                        && promotionApplicabilityService.isProductEligibleForPromotion(p, entity))
                .findFirst()
                .ifPresent(p -> {
                    String badge = promotionCalculator.generateBadgeText(p);
                    if (badge != null && !dto.getPromotionBadgeTexts().contains(badge)) {
                        dto.getPromotionBadgeTexts().add(badge);
                    }
                    dto.setIsSale(true);
                });
    }

    private <T extends PromotionEnrichable> T setNoDiscount(T dto) {
        dto.setOriginalPrice(dto.getPrice());
        dto.setDiscountAmount(BigDecimal.ZERO);
        dto.setDiscountPercentage(BigDecimal.ZERO);
        if (dto.getIsSale() == null || !dto.getIsSale()) {
            dto.setIsSale(false);
            dto.setPromotionBadgeTexts(new ArrayList<>());
        }
        return dto;
    }

    // Cache

    private PromotionIndex getCachedPromotionIndex() {
        LocalDateTime now = LocalDateTime.now();
        if (promotionIndex != null && cacheExpiry != null && now.isBefore(cacheExpiry)) {
            return promotionIndex;
        }
        synchronized (this) {
            if (promotionIndex != null && cacheExpiry != null && now.isBefore(cacheExpiry)) {
                return promotionIndex;
            }
            logger.info("Refreshing active promotions index from DB");
            List<CachedPromotion> activePromos = promotionRepository
                    .findActivePromotionsWithTargets(now)
                    .stream()
                    .map(CachedPromotion::from)
                    .toList();
            promotionIndex = new PromotionIndex(activePromos);
            cacheExpiry = now.plusMinutes(1);
            return promotionIndex;
        }
    }

    // Promotion Selection

    private Optional<CachedPromotion> findBestPromotion(ProductEntity product, PromotionIndex index) {
        Set<CachedPromotion> candidates = new HashSet<>(index.storewidePromos);

        candidates.addAll(index.pidToPromos.getOrDefault(product.getPid(), List.of()));

        if (product.getCategory() != null) {
            List<CachedPromotion> categoryPromos = index.categoryToPromos
                    .get(product.getCategory().getName().trim().toLowerCase());
            if (categoryPromos != null) {
                candidates.addAll(categoryPromos);
            }
        }

        if (product.getCollection() != null) {
            List<CachedPromotion> collectionPromos = index.collectionToPromos
                    .get(product.getCollection().getName().trim().toLowerCase());
            if (collectionPromos != null) {
                candidates.addAll(collectionPromos);
            }
        }

        if (!product.getTags().isEmpty()) {
            product.getTags().forEach(
                    tag -> candidates.addAll(index.tagToPromos.getOrDefault(tag.trim().toLowerCase(), List.of())));
        }

        BigDecimal originalPrice = product.getPrice();
        return candidates.stream()
                .filter(p -> promotionApplicabilityService.isApplicable(p, product, false))
                .max(Comparator
                        .comparing((CachedPromotion p) -> promotionCalculator.calculateDiscountAmount(p,
                                originalPrice, null))
                        .thenComparing(p -> p.type() == PromotionType.MEMBERSHIP_DISCOUNT ? 1 : 0));
    }
}

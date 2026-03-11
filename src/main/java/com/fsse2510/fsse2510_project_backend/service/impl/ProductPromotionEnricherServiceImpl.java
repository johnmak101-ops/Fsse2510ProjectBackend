package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductResponseData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductSummaryData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.PromotionEnrichable;
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
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
     *
     * Imagine you have 100 products on a page, and every product needs to check if
     * it has a discount.
     * If you ask the database 100 times, it's very slow (like walking to the store
     * 100 times).
     * Instead, we grab ALL the active discounts once, put them into sorting bins
     * (HashMaps) by
     * product ID, category, or tag. Then, checking a product's discount is instant
     * - just looking in the bin!
     */
    private static class PromotionIndex {
        final List<PromotionEntity> allPromos;
        final List<PromotionEntity> storewidePromos;
        final Map<Integer, List<PromotionEntity>> pidToPromos;
        final Map<String, List<PromotionEntity>> categoryToPromos;
        final Map<String, List<PromotionEntity>> collectionToPromos;
        final Map<String, List<PromotionEntity>> tagToPromos;

        PromotionIndex(List<PromotionEntity> promos) {
            this.allPromos = promos;
            this.storewidePromos = new ArrayList<>();
            this.pidToPromos = new HashMap<>();
            this.categoryToPromos = new HashMap<>();
            this.collectionToPromos = new HashMap<>();
            this.tagToPromos = new HashMap<>();

            List<PromotionEntity> sortedPromos = new ArrayList<>(promos);
            sortedPromos.sort((p1, p2) -> {
                BigDecimal v1 = p1.getDiscountValue() != null ? p1.getDiscountValue() : BigDecimal.ZERO;
                BigDecimal v2 = p2.getDiscountValue() != null ? p2.getDiscountValue() : BigDecimal.ZERO;
                return v2.compareTo(v1);
            });

            for (PromotionEntity p : sortedPromos) {
                if (p.getType() == PromotionType.STOREWIDE_SALE || p.getType() == PromotionType.MEMBERSHIP_DISCOUNT) {
                    storewidePromos.add(p);
                }
                if (p.getTargetPids() != null) {
                    p.getTargetPids().forEach(pid -> pidToPromos.computeIfAbsent(pid, k -> new ArrayList<>()).add(p));
                }
                if (p.getTargetCategories() != null) {
                    p.getTargetCategories().forEach(cat -> categoryToPromos
                            .computeIfAbsent(cat.trim().toLowerCase(), k -> new ArrayList<>()).add(p));
                }
                if (p.getTargetCollections() != null) {
                    p.getTargetCollections().forEach(col -> collectionToPromos
                            .computeIfAbsent(col.trim().toLowerCase(), k -> new ArrayList<>()).add(p));
                }
                if (p.getTargetTags() != null) {
                    p.getTargetTags().forEach(tag -> tagToPromos
                            .computeIfAbsent(tag.trim().toLowerCase(), k -> new ArrayList<>()).add(p));
                }
            }
        }

        boolean isEmpty() {
            return allPromos.isEmpty();
        }
    }

    // Public API
    /*
     * Enriches a list of ProductResponseData with the best applicable promotions.
     * Automatically fetches necessary ProductEntity records from the database.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseData> enrichWithPromotions(List<ProductResponseData> products) {
        if (products == null || products.isEmpty())
            return products;
        List<ProductEntity> entities = productRepository.findAllByPidIn(
                products.stream().map(ProductResponseData::getPid).toList());
        return enrichWithPromotions(products, entities);
    }

    /*
     * Enriches a list of ProductResponseData with the best applicable promotions,
     * using a pre-fetched list of ProductEntity records to optimize database hits.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseData> enrichWithPromotions(List<ProductResponseData> products,
            List<ProductEntity> entities) {
        return enrichList(products, entities, ProductResponseData::getPid);
    }

    /*
     * Enriches a list of ProductSummaryData with the best applicable promotions.
     * Automatically fetches necessary ProductEntity records from the database.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductSummaryData> enrichSummariesWithPromotions(List<ProductSummaryData> summaries) {
        if (summaries == null || summaries.isEmpty())
            return summaries;
        List<ProductEntity> entities = productRepository.findAllByPidIn(
                summaries.stream().map(ProductSummaryData::getPid).toList());
        return enrichSummariesWithPromotions(summaries, entities);
    }

    /*
     * Enriches a list of ProductSummaryData with the best applicable promotions,
     * using a pre-fetched list of ProductEntity records.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductSummaryData> enrichSummariesWithPromotions(List<ProductSummaryData> summaries,
            List<ProductEntity> entities) {
        return enrichList(summaries, entities, ProductSummaryData::getPid);
    }

    /*
     * Enriches a single ProductResponseData with the best applicable promotion.
     * Fetches the corresponding ProductEntity from the database.
     */
    @Override
    @Transactional(readOnly = true)
    public ProductResponseData enrichWithPromotions(ProductResponseData product) {
        if (product == null)
            return null;
        ProductEntity entity = productRepository.findByPidWithAllDetails(product.getPid()).orElse(null);
        return enrichWithPromotions(product, entity);
    }

    /*
     * Enriches a single ProductResponseData with the best applicable promotion,
     * using a pre-fetched ProductEntity.
     */
    @Override
    @Transactional(readOnly = true)
    public ProductResponseData enrichWithPromotions(ProductResponseData product, ProductEntity entity) {
        if (product == null)
            return null;
        PromotionIndex index = getCachedPromotionIndex();
        if (index == null || index.isEmpty())
            return setNoDiscount(product);
        return applyBestPromotion(product, index, entity);
    }

    /*
     * Manually clears the active promotions index cache.
     * Thread-safe operation to reset the volatile cache variables.
     */
    @Override
    public void clearCache() {
        synchronized (this) {
            promotionIndex = null;
            cacheExpiry = null;
            logger.info("ProductPromotionEnricherService cache cleared manually.");
        }
    }

    // Generic Core Logic

    /*
     * Generic enrichment for any list of PromotionEnrichable DTOs.
     *
     * Our system has multiple DTOs that represent products (e.g., Summary, Detail).
     * By having them all implement `PromotionEnrichable`, we can run the EXACT same
     * discount calculation logic on all of them. No more copying and pasting the
     * math code. If a bug is fixed here, it's fixed everywhere.
     */
    private <T extends PromotionEnrichable> List<T> enrichList(List<T> dtos,
            List<ProductEntity> entities,
            Function<T, Integer> pidExtractor) {
        if (dtos == null || dtos.isEmpty())
            return dtos;

        PromotionIndex index = getCachedPromotionIndex();
        if (index == null || index.isEmpty()) {
            return dtos.stream().map(this::setNoDiscount).toList();
        }

        Map<Integer, ProductEntity> entityMap = entities.stream()
                .collect(Collectors.toMap(ProductEntity::getPid, Function.identity()));

        return dtos.stream()
                .map(dto -> applyBestPromotion(dto, index, entityMap.get(pidExtractor.apply(dto))))
                .toList();
    }

    /*
     * Apply the best promotion to any PromotionEnrichable DTO.
     * Replaces the duplicate applyBestPromotionWithEntity +
     * applyBestPromotionToSummaryWithEntity pair.
     */
    private <T extends PromotionEnrichable> T applyBestPromotion(T dto,
            PromotionIndex index,
            ProductEntity entity) {
        if (entity == null)
            return setNoDiscount(dto);

        dto.setPromotionBadgeTexts(new ArrayList<>());
        dto.setIsSale(false);

        Optional<PromotionEntity> best = findBestPromotion(entity, index);

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

        if (PRICE_REDUCING_TYPES.contains(best.get().getType())) {
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

    /*
     * Append membership teaser badge if applicable (does not overwrite existing
     * badges).
     */
    private <T extends PromotionEnrichable> void checkAndSetMembershipBadge(T dto,
            List<PromotionEntity> activePromos,
            ProductEntity entity) {
        activePromos.stream()
                .filter(p -> p.getType() == PromotionType.MEMBERSHIP_DISCOUNT
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

    /*
     * Clear all promotion-related fields. Works for any PromotionEnrichable DTO.
     */
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

    /*
     * Retrieves the cached active promotions index, or refreshes it from the DB if
     * expired.
     *
     * Under heavy load, if the cache expires, 1000 users hitting the site might all
     * trigger
     * a slow database fetch at the exact same split second (called a Cache
     * Stampede).
     * The `synchronized(this)` block ensures only ONE thread fetches the data, and
     * everyone
     * else waits for that thread to finish and shares the updated data.
     */
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
            List<PromotionEntity> activePromos = promotionRepository.findActivePromotionsWithTargets(now);
            promotionIndex = new PromotionIndex(activePromos);
            cacheExpiry = now.plusMinutes(1);
            return promotionIndex;
        }
    }

    // Promotion Selection

    /*
     * Determines the single best promotion for a product out of all applicable
     * candidates.
     *
     * We grab only the promotions from the 'bins' (HashMaps) that directly match
     * this product's
     * attributes. Then we calculate the actual $ discount amount for each
     * candidate, and finally pick the MAX discount. This guarantees the customer
     * always gets the cheapest
     * possible price.
     */
    private Optional<PromotionEntity> findBestPromotion(ProductEntity product, PromotionIndex index) {
        Set<PromotionEntity> candidates = new HashSet<>(index.storewidePromos);

        candidates.addAll(index.pidToPromos.getOrDefault(product.getPid(), List.of()));

        if (product.getCategory() != null) {
            List<PromotionEntity> categoryPromos = index.categoryToPromos
                    .get(product.getCategory().getName().trim().toLowerCase());
            if (categoryPromos != null) {
                candidates.addAll(categoryPromos);
            }
        }

        if (product.getCollection() != null) {
            List<PromotionEntity> collectionPromos = index.collectionToPromos
                    .get(product.getCollection().getName().trim().toLowerCase());
            if (collectionPromos != null) {
                candidates.addAll(collectionPromos);
            }
        }

        if (product.getTags() != null) {
            product.getTags().forEach(
                    tag -> candidates.addAll(index.tagToPromos.getOrDefault(tag.trim().toLowerCase(), List.of())));
        }

        BigDecimal originalPrice = product.getPrice(); // Define originalPrice here for use in comparator
        return candidates.stream()
                .filter(p -> promotionApplicabilityService.isApplicable(p, product, false))
                .max(Comparator
                        .comparing((PromotionEntity p) -> promotionCalculator.calculateDiscountAmount(p,
                                originalPrice, null))
                        .thenComparing(p -> p.getType() == PromotionType.MEMBERSHIP_DISCOUNT ? 1 : 0));
    }
}

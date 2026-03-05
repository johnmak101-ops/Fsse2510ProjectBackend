package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.response.CartItemResponseData;
import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
import com.fsse2510.fsse2510_project_backend.data.promotion.promotionType.PromotionType;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import com.fsse2510.fsse2510_project_backend.repository.ProductRepository;
import com.fsse2510.fsse2510_project_backend.repository.PromotionRepository;
import com.fsse2510.fsse2510_project_backend.service.CartPromotionEnricherService;
import com.fsse2510.fsse2510_project_backend.service.PromotionApplicabilityService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartPromotionEnricherServiceImpl implements CartPromotionEnricherService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final PromotionApplicabilityService promotionApplicabilityService;
    private final PromotionCalculator promotionCalculator;

    private final Logger logger = LoggerFactory.getLogger(CartPromotionEnricherServiceImpl.class);
    /*
     * Ensure each thread use the updated promotion, no cpu cache read
     * Write index in main memory, prevent NullPointerException
     * Solved stale promotion tag and calculation mismatch in previous version
     * Memory Barrier + Prevent Reordering
     */
    private volatile PromotionIndex promotionIndex;
    private volatile LocalDateTime cacheExpiry;

    /*
     * Enhance the promotion calculation, avoid nested for loop
     * Prevent situations in previous version
     * Where I have 50 stock in cart & 50 promotion active,
     * then 50*50 times calculations
     * This version use Constructor class to save all the promotion info
     * Create O(1) index dictionary and do sorting, ensure the best promo applied to
     * products
     */
    private static class PromotionIndex {
        final List<PromotionEntity> allPromos; // For isEmpty() check, bypass calculation if empty
        final List<PromotionEntity> regularPromos;
        final List<PromotionEntity> bundlePromos;
        final List<PromotionEntity> orderLevelPromos;
        final List<PromotionEntity> bxgyPromos;

        final Map<Integer, List<PromotionEntity>> pidToPromos;
        final Map<String, List<PromotionEntity>> categoryToPromos;
        final Map<String, List<PromotionEntity>> collectionToPromos;
        final Map<String, List<PromotionEntity>> tagToPromos;
        final List<PromotionEntity> storewidePromos;

        // Big Filter
        private static final EnumSet<PromotionType> NON_REGULAR_TYPES = EnumSet.of(
                PromotionType.BUY_X_GET_Y_FREE,
                PromotionType.BUNDLE_DISCOUNT,
                PromotionType.MIN_QUANTITY_DISCOUNT,
                PromotionType.MIN_AMOUNT_DISCOUNT);

        // Order Level Filter
        private static final EnumSet<PromotionType> ORDER_LEVEL_TYPES = EnumSet.of(
                PromotionType.MIN_QUANTITY_DISCOUNT,
                PromotionType.MIN_AMOUNT_DISCOUNT);

        PromotionIndex(List<PromotionEntity> promos) {
            this.allPromos = promos;
            this.regularPromos = new ArrayList<>();
            this.bundlePromos = new ArrayList<>();
            this.orderLevelPromos = new ArrayList<>();
            this.bxgyPromos = new ArrayList<>();
            this.pidToPromos = new HashMap<>(); // Key Value Pair
            this.categoryToPromos = new HashMap<>(); // Key Value Pair
            this.collectionToPromos = new HashMap<>(); // Key Value Pair
            this.tagToPromos = new HashMap<>(); // Key Value Pair
            this.storewidePromos = new ArrayList<>();

            for (PromotionEntity p : promos) {
                PromotionType type = p.getType();

                // Group by promo category
                if (!NON_REGULAR_TYPES.contains(type)) {
                    regularPromos.add(p);
                } else if (type == PromotionType.BUNDLE_DISCOUNT) {
                    bundlePromos.add(p);
                } else if (ORDER_LEVEL_TYPES.contains(type)) {
                    orderLevelPromos.add(p);
                } else if (type == PromotionType.BUY_X_GET_Y_FREE) {
                    bxgyPromos.add(p);
                }

                // Sub-index for regular promos
                if (!NON_REGULAR_TYPES.contains(type)) {
                    if (type == PromotionType.STOREWIDE_SALE) {
                        storewidePromos.add(p);
                    }
                    indexTargets(p);
                }
            }

            // Sort all index lists by discount DESC
            Comparator<PromotionEntity> byDiscountDesc = Comparator.comparing(
                    (PromotionEntity p) -> p.getDiscountValue() != null ? p.getDiscountValue() : BigDecimal.ZERO)
                    .reversed();

            pidToPromos.values().forEach(list -> list.sort(byDiscountDesc));
            categoryToPromos.values().forEach(list -> list.sort(byDiscountDesc));
            collectionToPromos.values().forEach(list -> list.sort(byDiscountDesc));
            tagToPromos.values().forEach(list -> list.sort(byDiscountDesc));
            storewidePromos.sort(byDiscountDesc);
        }

        // Put result in the buckets
        private void indexTargets(PromotionEntity p) {
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
                p.getTargetTags().forEach(
                        tag -> tagToPromos.computeIfAbsent(tag.trim().toLowerCase(), k -> new ArrayList<>()).add(p));
            }
        }

        boolean isEmpty() {
            return allPromos.isEmpty();
        }
    }

    // Resolve original price
    private static BigDecimal resolveOriginal(CartItemResponseData item) {
        return item.getOriginalPrice() != null ? item.getOriginalPrice() : item.getPrice();
    }

    // Apply pricing fields to an existing item (mutate in place)
    private void applyPricing(CartItemResponseData item, BigDecimal originalPrice,
            BigDecimal newPrice, PromotionEntity promo) {
        BigDecimal discountAmount = originalPrice.subtract(newPrice);
        BigDecimal discountPct = originalPrice.compareTo(BigDecimal.ZERO) > 0
                ? discountAmount.divide(originalPrice, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        item.setPrice(newPrice);
        item.setOriginalPrice(originalPrice);
        item.setDiscountAmount(discountAmount);
        item.setDiscountPercentage(discountPct);

        // Add badge and ID to lists instead of replacing them completely
        String badge = promotionCalculator.generateBadgeText(promo);
        if (badge != null && !item.getPromotionBadgeTexts().contains(badge)) {
            item.getPromotionBadgeTexts().add(badge);
        }
        if (promo.getId() != null && !item.getAppliedPromotionIds().contains(promo.getId())) {
            item.getAppliedPromotionIds().add(promo.getId());
        }
    }

    // Apply additive pricing for order-level promotions (stacking on top)
    private void additiveApplyPricing(CartItemResponseData item, BigDecimal currentNetPrice,
            BigDecimal discountToApply, PromotionEntity promo) {
        BigDecimal newNetPrice = currentNetPrice.subtract(discountToApply).max(BigDecimal.ZERO);
        BigDecimal originalPrice = resolveOriginal(item); // Keep Phase 1 original

        // Add to existing discount amount
        BigDecimal newDiscountAmount = item.getDiscountAmount() != null
                ? item.getDiscountAmount().add(discountToApply)
                : discountToApply;

        // Recalculate percentage based on original price
        BigDecimal newDiscountPct = originalPrice.compareTo(BigDecimal.ZERO) > 0
                ? newDiscountAmount.divide(originalPrice, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        item.setPrice(newNetPrice);
        item.setDiscountAmount(newDiscountAmount);
        item.setDiscountPercentage(newDiscountPct);

        // Append to existing badgs/IDs
        String badge = promotionCalculator.generateBadgeText(promo);
        if (badge != null && !item.getPromotionBadgeTexts().contains(badge)) {
            item.getPromotionBadgeTexts().add(badge);
        }
        if (promo.getId() != null && !item.getAppliedPromotionIds().contains(promo.getId())) {
            item.getAppliedPromotionIds().add(promo.getId());
        }
    }

    // Clear all discount fields (mutate in place)
    private static void clearDiscount(CartItemResponseData item) {
        BigDecimal original = resolveOriginal(item);
        item.setPrice(original);
        item.setOriginalPrice(original);
        item.setDiscountAmount(BigDecimal.ZERO);
        item.setDiscountPercentage(BigDecimal.ZERO);
        item.getPromotionBadgeTexts().clear();
        item.getAppliedPromotionIds().clear();
    }

    // For Service use

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponseData> enrichWithPromotions(List<CartItemResponseData> cartItems, UserEntity user) {
        if (cartItems == null || cartItems.isEmpty())
            return cartItems;

        PromotionIndex index = getCachedPromotionIndex();
        if (index == null || index.isEmpty()) {
            cartItems.forEach(CartPromotionEnricherServiceImpl::clearDiscount);
            return cartItems;
        }

        /*
         * Fetch all at once, prevent N+1
         * Pid and product entity key-value pair
         * All pids are unique in cart, all qty are merged to one entry
         * IllegalStateException: Duplicate key will not happen in
         * Function.identity(returns its input argument)
         * Merge Function not required ((existing, replacement) -> existing)
         */
        List<Integer> pids = cartItems.stream().map(CartItemResponseData::getPid).toList();
        Map<Integer, ProductEntity> entityMap = productRepository.findAllByPidIn(pids).stream()
                .collect(Collectors.toMap(ProductEntity::getPid, Function.identity()));

        // Pass 1: per-item regular promos, apply first
        cartItems.forEach(item -> applyBestRegularPromo(item, index, entityMap.get(item.getPid()), user));

        // Pass 2: bundle promos, can replace
        if (!index.bundlePromos.isEmpty()) {
            applyBundlePromos(cartItems, index.bundlePromos, entityMap, user);
        }

        // Pass 3: buy-X-get-Y promos, can replace
        if (!index.bxgyPromos.isEmpty()) {
            applyBuyXGetYPromos(cartItems, index.bxgyPromos, entityMap, user);
        }

        // Pass 4: order-level promos (Stacked on top of Pass 1/2/3)
        if (!index.orderLevelPromos.isEmpty()) {
            applyOrderLevelPromos(cartItems, index.orderLevelPromos, entityMap, user);
        }

        return cartItems;
    }

    @Override
    public void clearCache() {
        synchronized (this) {
            promotionIndex = null;
            cacheExpiry = null;
            logger.info("CartPromotionEnricherService cache cleared manually.");
        }
    }

    // Pass 1: Regular Promos

    private void applyBestRegularPromo(CartItemResponseData item, PromotionIndex index,
            ProductEntity productEntity, UserEntity user) {
        if (productEntity == null)
            return;

        BigDecimal originalPrice = resolveOriginal(item);

        // Gather candidates via index
        // Promotion unique, use set
        /*
         * Logic:
         * 1. Add pid = 120
         * 2. item.getpid() -> 120
         * 3. pidToPromos -> Check Promotion? addAll: List.of()
         * Prevent Null Pointer Exception
         */
        Set<PromotionEntity> candidates = new HashSet<>(index.storewidePromos);
        candidates.addAll(index.pidToPromos.getOrDefault(item.getPid(), List.of()));

        // Database may have null value, and casing issues
        Optional.ofNullable(productEntity.getCategory())
                .map(c -> index.categoryToPromos.get(c.getName().trim().toLowerCase()))
                .ifPresent(candidates::addAll);

        Optional.ofNullable(productEntity.getCollection())
                .map(c -> index.collectionToPromos.get(c.getName().trim().toLowerCase()))
                .ifPresent(candidates::addAll);

        // List<String>
        if (productEntity.getTags() != null) {
            productEntity.getTags().forEach(
                    tag -> candidates.addAll(index.tagToPromos.getOrDefault(tag.trim().toLowerCase(), List.of())));
        }

        // Find best promo with stream().max() + tie-breaker
        /*
         * Logic:
         * 1. filter active promo
         * 2. max promo
         * 3. Member Discount OR Normal Discount
         */
        Optional<PromotionEntity> best = candidates.stream()
                .filter(p -> promotionApplicabilityService.isApplicable(p, productEntity, user, true))
                .max(Comparator
                        .comparing((PromotionEntity p) -> promotionCalculator.calculateDiscountAmount(p, originalPrice))
                        .thenComparing(p -> p.getType() == PromotionType.MEMBERSHIP_DISCOUNT ? 1 : 0));

        if (best.isPresent()) {
            BigDecimal promoPrice = promotionCalculator.calculatePromotionalPrice(originalPrice, best.get());
            applyPricing(item, originalPrice, promoPrice, best.get());
        } else {
            checkAndSetMembershipBadge(item, index.regularPromos, productEntity);
        }
    }

    // Bundle Promos
    private void applyBundlePromos(List<CartItemResponseData> items,
            List<PromotionEntity> bundlePromos,
            Map<Integer, ProductEntity> entityMap, UserEntity user) {
        for (PromotionEntity promo : bundlePromos) {
            int minQty = promo.getMinQuantity() != null ? promo.getMinQuantity() : 0;

            List<CartItemResponseData> qualifying = items.stream()
                    .filter(item -> {
                        ProductEntity pe = entityMap.get(item.getPid());
                        return pe != null && promotionApplicabilityService.isApplicable(promo, pe, user, true);
                    })
                    .toList();

            if (qualifying.isEmpty())
                continue;

            int totalQty = qualifying.stream().mapToInt(CartItemResponseData::getCartQuantity).sum();
            if (totalQty < minQty)
                continue;

            if (promo.getDiscountType() == DiscountType.PERCENTAGE) {
                // Apply if better than existing discount for each item
                // NOTE: This does NOT support repeating bundles (Multipliers) out-of-the-box.
                // e.g. "Buy 6 Get $60" when promo is "Buy 3 Get $30".
                // To support circular/repeating bundles, an `isRepeating` boolean flag is
                // needed on PromotionEntity.
                for (CartItemResponseData item : qualifying) {
                    BigDecimal originalPrice = resolveOriginal(item);
                    BigDecimal bundleDiscount = promotionCalculator.calculateDiscountAmount(promo, originalPrice);
                    BigDecimal currentDiscount = item.getDiscountAmount() != null ? item.getDiscountAmount()
                            : BigDecimal.ZERO;

                    if (bundleDiscount.compareTo(currentDiscount) > 0) {
                        BigDecimal promoPrice = promotionCalculator.calculatePromotionalPrice(originalPrice, promo);
                        applyPricing(item, originalPrice, promoPrice, promo);
                    }
                }
            } else if (promo.getDiscountType() == DiscountType.FIXED) {
                // Apply the fixed discount ONCE across the qualifying bundle items
                // proportionally
                // NOTE: This does NOT support repeating bundles (Multipliers) out-of-the-box.
                // e.g. "Buy 6 Get $60" when promo is "Buy 3 Get $30".
                // To support circular/repeating bundles, an `isRepeating` boolean flag is
                // needed on PromotionEntity.
                distributeFixedDiscount(qualifying, promo, false);
            }
        }
    }

    // Order-Level Promos
    private void applyOrderLevelPromos(List<CartItemResponseData> items,
            List<PromotionEntity> orderLevelPromos,
            Map<Integer, ProductEntity> entityMap, UserEntity user) {
        for (PromotionEntity promo : orderLevelPromos) {
            List<CartItemResponseData> qualifying = items.stream()
                    .filter(item -> {
                        ProductEntity pe = entityMap.get(item.getPid());
                        return pe != null && promotionApplicabilityService.isApplicable(promo, pe, user, true);
                    })
                    .toList();

            if (qualifying.isEmpty())
                continue;

            boolean conditionMet = false;

            // Check Qty
            if (promo.getType() == PromotionType.MIN_QUANTITY_DISCOUNT) {
                int minQty = promo.getMinQuantity() != null ? promo.getMinQuantity() : 0;
                int totalQty = qualifying.stream().mapToInt(CartItemResponseData::getCartQuantity).sum();
                conditionMet = totalQty >= minQty;
                logger.debug("MIN_QTY promo {}: totalQty={}, minQty={}, met={}", promo.getId(), totalQty, minQty,
                        conditionMet);

                // Check total cart amount (using PHASE 1 NET PRICE for order-level stacking)
            } else if (promo.getType() == PromotionType.MIN_AMOUNT_DISCOUNT) {
                BigDecimal minAmount = promo.getMinAmount() != null ? promo.getMinAmount() : BigDecimal.ZERO;
                BigDecimal totalAmount = qualifying.stream()
                        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getCartQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                conditionMet = totalAmount.compareTo(minAmount) >= 0;
                logger.debug("MIN_AMOUNT promo {}: totalAmount={}, minAmount={}, met={}", promo.getId(), totalAmount,
                        minAmount, conditionMet);
            }

            if (!conditionMet) {
                logger.info("Order-level promo {} condition NOT met", promo.getId());
                continue;
            }

            logger.info("Order-level promo {} condition met. Qualifying items: {}", promo.getId(), qualifying.size());

            if (promo.getDiscountType() == DiscountType.PERCENTAGE) {
                for (CartItemResponseData item : qualifying) {
                    BigDecimal netPrice = item.getPrice(); // Stacked on Phase 1 net price

                    // Additive calculating
                    BigDecimal promoDiscount = promotionCalculator.calculateDiscountAmount(promo, netPrice);

                    if (promoDiscount.compareTo(BigDecimal.ZERO) > 0) {
                        logger.info("Applying additive percentage promo {} to SKU {}", promo.getId(), item.getSku());
                        additiveApplyPricing(item, netPrice, promoDiscount, promo);
                    }
                }
            } else if (promo.getDiscountType() == DiscountType.FIXED) {
                distributeFixedDiscount(qualifying, promo, true); // true = use Net Price
            }
        }
    }

    /*
     * Proportional Allocation strategy for FIXED discounts.
     * 
     * WHY THIS IS NEEDED:
     * If a promo is "Get $50 OFF the order" and a customer buys item A ($80) and
     * item B ($20),
     * we cannot just deduct the entire $50 from item A. If the customer refunds
     * item A,
     * they would be refunded $30, and keep item B ($20 value) having only paid $50
     * total,
     * effectively getting item B for free.
     * Proportional allocation spreads the discount based on the item's weight in
     * the cart subtotal.
     * (Item A gets 80% of the $50 discount, Item B gets 20%).
     * 
     * PENNY PROBLEM SOLVER:
     * When dividing fixed discounts across items (e.g., $10 off three $10 items),
     * $10 / 3 = $3.33 discount per item.
     * $3.33 + $3.33 + $3.33 = $9.99 total distributed discount.
     * We lose $0.01 due to rounding (The Penny Problem).
     * To solve this, we use the "Last Item Absorbs" pattern:
     * The last item in the list ignores proportional math and just absorbs whatever
     * discount is left.
     */
    private void distributeFixedDiscount(List<CartItemResponseData> qualifying, PromotionEntity promo,
            boolean useNetPrice) {
        BigDecimal totalDiscount = promo.getDiscountValue();
        if (totalDiscount == null || totalDiscount.compareTo(BigDecimal.ZERO) <= 0)
            return;

        // 1. Calculate overall total amount of qualifying items (using cart quantity)
        // This is the denominator for our proportional weight calculations.
        BigDecimal totalQualifyingAmount = qualifying.stream()
                .map(item -> {
                    BigDecimal basePrice = useNetPrice ? item.getPrice() : resolveOriginal(item);
                    return basePrice.multiply(BigDecimal.valueOf(item.getCartQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalQualifyingAmount.compareTo(BigDecimal.ZERO) <= 0)
            return;

        // 2. Prevent the company from paying the customer.
        // If the cart is $30 and the coupon is $50, we can only deduct $30.
        BigDecimal actualDiscountToApply = totalDiscount.min(totalQualifyingAmount);

        // Check if applying this fixed discount is actually better than their CURRENT
        // overall discounts (ONLY for bundle promos, order-level stacks additively!).
        if (!useNetPrice) {
            BigDecimal currentTotalDiscount = qualifying.stream()
                    .map(item -> (item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO)
                            .multiply(BigDecimal.valueOf(item.getCartQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (actualDiscountToApply.compareTo(currentTotalDiscount) <= 0) {
                logger.info("Fixed promo {} offers {}, but current discount {} is better or equal. Skipping.",
                        promo.getId(), actualDiscountToApply, currentTotalDiscount);
                return;
            }
        }

        BigDecimal distributedDiscount = BigDecimal.ZERO;

        for (int i = 0; i < qualifying.size(); i++) {
            CartItemResponseData item = qualifying.get(i);
            int qty = item.getCartQuantity();
            BigDecimal baseUnitPrice = useNetPrice ? item.getPrice() : resolveOriginal(item);
            BigDecimal itemTotalBase = baseUnitPrice.multiply(BigDecimal.valueOf(qty));

            BigDecimal itemDiscountTotal;

            // 3. The Penny Problem Solver: Last item absorbs the remainder.
            if (i == qualifying.size() - 1) {
                // Ignore proportionality, just dump whatever discount hasn't been used yet.
                itemDiscountTotal = actualDiscountToApply.subtract(distributedDiscount);
            } else {
                // 4. Proportional Allocation: (itemTotal / overallTotal) *
                // actualDiscountToApply
                // Multiply first, divide last — preserves precision.
                itemDiscountTotal = itemTotalBase.multiply(actualDiscountToApply)
                        .divide(totalQualifyingAmount, 2, RoundingMode.HALF_UP);

                // Keep track of how much discount we've handed out so far
                distributedDiscount = distributedDiscount.add(itemDiscountTotal);
            }

            if (useNetPrice) {
                // Additive stack on top of net price
                // itemDiscountTotal refers to the total amount to take off this cart item
                // group.
                // We divide it evenly amongst all qty in this line item.
                BigDecimal singleUnitDiscount = itemDiscountTotal.divide(BigDecimal.valueOf(qty), 2,
                        RoundingMode.HALF_UP);
                additiveApplyPricing(item, baseUnitPrice, singleUnitDiscount, promo);
            } else {
                // 5. Calculate new unit price
                // Ensure itemTotal doesn't drop below 0
                BigDecimal newItemTotal = itemTotalBase.subtract(itemDiscountTotal).max(BigDecimal.ZERO);

                // Divide the final discounted total by the quantity to get the discounted
                // single unit price
                BigDecimal newUnitPrice = newItemTotal.divide(BigDecimal.valueOf(qty), 2, RoundingMode.HALF_UP);

                // Mutate the item state to reflect the promotion
                applyPricing(item, baseUnitPrice, newUnitPrice, promo);
            }

            logger.info("Proportionally distributed {} from fixed promo {} to SKU {}.",
                    itemDiscountTotal, promo.getId(), item.getSku());
        }
    }

    // Buy X Get Y
    private void applyBuyXGetYPromos(List<CartItemResponseData> items,
            List<PromotionEntity> b2gyPromos,
            Map<Integer, ProductEntity> entityMap, UserEntity user) {
        Map<Integer, List<CartItemResponseData>> eligibleItemsMap = new HashMap<>();

        for (CartItemResponseData item : items) {
            ProductEntity pe = entityMap.get(item.getPid());
            if (pe == null)
                continue;
            for (PromotionEntity promo : b2gyPromos) {
                if (promotionApplicabilityService.isApplicable(promo, pe, user, true)) {
                    eligibleItemsMap.computeIfAbsent(promo.getId(), k -> new ArrayList<>()).add(item);
                }
            }
        }

        for (Map.Entry<Integer, List<CartItemResponseData>> entry : eligibleItemsMap.entrySet()) {
            PromotionEntity promo = b2gyPromos.stream()
                    .filter(p -> p.getId().equals(entry.getKey())).findFirst().orElse(null);
            if (promo == null)
                continue;

            List<CartItemResponseData> eligible = entry.getValue();
            int bundleSize = promo.getBuyX() + promo.getGetY();
            int totalQty = eligible.stream().mapToInt(CartItemResponseData::getCartQuantity).sum();
            if (totalQty < bundleSize)
                continue;

            int totalFreeItems = (totalQty / bundleSize) * promo.getGetY();

            // Sort by price ascending — discount cheapest items first
            eligible.sort(Comparator.comparing(CartPromotionEnricherServiceImpl::resolveOriginal));

            int remainingFree = totalFreeItems;
            for (CartItemResponseData item : eligible) {
                if (remainingFree <= 0)
                    break;

                int qty = item.getCartQuantity();
                BigDecimal unitPrice = resolveOriginal(item);
                int reduceCount = Math.min(qty, remainingFree);

                if (reduceCount > 0) {
                    BigDecimal oldTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
                    BigDecimal freeAmount = unitPrice.multiply(BigDecimal.valueOf(reduceCount));
                    BigDecimal newUnitPrice = oldTotal.subtract(freeAmount)
                            .divide(BigDecimal.valueOf(qty), 2, RoundingMode.HALF_UP);

                    // Only apply if better than current price
                    if (newUnitPrice.compareTo(item.getPrice()) < 0) {
                        applyPricing(item, unitPrice, newUnitPrice, promo);
                    }
                    remainingFree -= reduceCount;
                }
            }
        }
    }

    // Membership Badge

    private void checkAndSetMembershipBadge(CartItemResponseData item, List<PromotionEntity> activePromos,
            ProductEntity productEntity) {
        activePromos.stream()
                .filter(p -> p.getType() == PromotionType.MEMBERSHIP_DISCOUNT
                        && promotionApplicabilityService.isProductEligibleForPromotion(p, productEntity))
                .findFirst()
                .ifPresent(p -> {
                    String badge = promotionCalculator.generateBadgeText(p);
                    if (badge != null && !item.getPromotionBadgeTexts().contains(badge)) {
                        item.getPromotionBadgeTexts().add(badge);
                    }
                    if (p.getId() != null && !item.getAppliedPromotionIds().contains(p.getId())) {
                        item.getAppliedPromotionIds().add(p.getId());
                    }
                });
    }

    /*
     * Prevent cache stampede, custom expiry time and zero dependency
     * Another option is to use @Cacheable(value = "promotions", key = "'index'",
     * sync = true) on Redis
     * Considering promotion index is a super hot path\
     * Local cache can enhance performance
     */
    private PromotionIndex getCachedPromotionIndex() {
        LocalDateTime now = LocalDateTime.now();
        // 1st Check
        if (promotionIndex != null && cacheExpiry != null && now.isBefore(cacheExpiry)) {
            return promotionIndex;
        }
        // prevent fetching db twice, lock only 1 thread on this service
        // class(Singleton)
        synchronized (this) {
            // Check again to prevent other threads to pull db again
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
}

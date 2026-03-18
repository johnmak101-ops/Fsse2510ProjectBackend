package com.fsse2510.fsse2510_project_backend.data.promotion.cache;

import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
import com.fsse2510.fsse2510_project_backend.data.promotion.promotionType.PromotionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA-free snapshot of a PromotionEntity for safe caching outside Hibernate sessions.
 * Deep-copies all @ElementCollection sets so no lazy proxy survives.
 */
public record CachedPromotion(
        Integer id,
        String name,
        String description,
        PromotionType type,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer minQuantity,
        BigDecimal minAmount,
        MembershipLevel targetMemberLevel,
        Set<Integer> targetPids,
        Set<String> targetCategories,
        Set<String> targetCollections,
        Set<String> targetTags,
        DiscountType discountType,
        BigDecimal discountValue,
        Integer buyX,
        Integer getY) {

    /**
     * Deep-copy factory — must be called inside an open Hibernate session
     * (i.e. within @Transactional) so that lazy collections are initialised.
     */
    public static CachedPromotion from(PromotionEntity e) {
        return new CachedPromotion(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getType(),
                e.getStartDate(),
                e.getEndDate(),
                e.getMinQuantity(),
                e.getMinAmount(),
                e.getTargetMemberLevel(),
                e.getTargetPids() != null ? new HashSet<>(e.getTargetPids()) : Set.of(),
                e.getTargetCategories() != null ? new HashSet<>(e.getTargetCategories()) : Set.of(),
                e.getTargetCollections() != null ? new HashSet<>(e.getTargetCollections()) : Set.of(),
                e.getTargetTags() != null ? new HashSet<>(e.getTargetTags()) : Set.of(),
                e.getDiscountType(),
                e.getDiscountValue(),
                e.getBuyX(),
                e.getGetY());
    }
}

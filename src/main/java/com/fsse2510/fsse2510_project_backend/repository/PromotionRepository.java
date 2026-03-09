package com.fsse2510.fsse2510_project_backend.repository;

import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
import com.fsse2510.fsse2510_project_backend.data.promotion.promotionType.PromotionType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<PromotionEntity, Integer> {

        @EntityGraph(attributePaths = { "targetPids", "targetCategories", "targetCollections", "targetTags" })
        List<PromotionEntity> findDistinctByTypeIn(List<PromotionType> types);

        @EntityGraph(attributePaths = { "targetPids", "targetCategories", "targetCollections", "targetTags" })
        @Query("SELECT DISTINCT p FROM PromotionEntity p WHERE p.startDate <= :now AND (p.endDate IS NULL OR p.endDate > :now)")
        List<PromotionEntity> findActivePromotions(@Param("now") LocalDateTime now);

        @EntityGraph(attributePaths = { "targetPids", "targetCategories", "targetCollections", "targetTags" })
        @Query("SELECT DISTINCT p FROM PromotionEntity p WHERE p.startDate <= :now AND (p.endDate IS NULL OR p.endDate > :now)")
        List<PromotionEntity> findActivePromotionsWithTargets(@Param("now") LocalDateTime now);

        @EntityGraph(attributePaths = { "targetPids", "targetCategories", "targetCollections", "targetTags" })
        @Query("SELECT DISTINCT p FROM PromotionEntity p WHERE p.type IN :types AND p.startDate <= :now AND (p.endDate IS NULL OR p.endDate > :now)")
        List<PromotionEntity> findActiveByType(@Param("types") List<PromotionType> types,
                        @Param("now") LocalDateTime now);

        @Query("SELECT p FROM PromotionEntity p WHERE p.endDate IS NOT NULL AND p.endDate <= :now")
        List<PromotionEntity> findExpiredPromotions(@Param("now") LocalDateTime now);
}
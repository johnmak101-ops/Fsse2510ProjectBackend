package com.fsse2510.fsse2510_project_backend.repository;

import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository
                extends JpaRepository<ProductEntity, Integer>, JpaSpecificationExecutor<ProductEntity> {

        @Query("SELECT DISTINCT p FROM ProductEntity p LEFT JOIN FETCH p.inventories LEFT JOIN FETCH p.category LEFT JOIN FETCH p.collection LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.images WHERE p.slug = :slug")
        Optional<ProductEntity> findBySlug(@Param("slug") String slug);

        @Query("SELECT DISTINCT p FROM ProductEntity p LEFT JOIN FETCH p.inventories LEFT JOIN FETCH p.category LEFT JOIN FETCH p.collection LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.images WHERE p.pid = :pid")
        Optional<ProductEntity> findByPidWithAllDetails(@Param("pid") Integer pid);

        @Modifying
        @Query("UPDATE ProductEntity p SET p.promotion = NULL, p.isSale = false, p.promotionBadgeText = NULL WHERE p.promotion.id = :promoId")
        void clearPromotionFromProducts(@Param("promoId") Integer promoId);

        @Query("SELECT DISTINCT i.size FROM ProductInventoryEntity i WHERE i.size IS NOT NULL")
        List<String> findAllDistinctSizes();

        @Query("SELECT DISTINCT i.color FROM ProductInventoryEntity i WHERE i.color IS NOT NULL")
        List<String> findAllDistinctColors();

        @Query("SELECT DISTINCT p.productType FROM ProductEntity p WHERE p.productType IS NOT NULL")
        List<String> findAllDistinctProductTypes();

        @Query(value = "SELECT DISTINCT tag FROM product_tags", nativeQuery = true)
        List<String> findAllDistinctTags();

        @Query("SELECT p.pid FROM ProductEntity p JOIN p.category c WHERE c.name = :category AND p.pid <> :pid")
        Slice<Integer> findResultIdsByCategoryAndPidNot(@Param("category") String category, @Param("pid") Integer pid,
                        Pageable pageable);

        // Secondary sort by pid ensures stable ordering when featured/new values are equal
        @Query("SELECT p.pid FROM ProductEntity p ORDER BY p.isFeatured DESC, p.featuredPriority DESC, p.isNew DESC, p.pid DESC")
        Slice<Integer> findShowcaseProductIds(Pageable pageable);

        // ORDER BY pid DESC ensures stable, consistent pagination results across requests
        @Query("SELECT p.pid FROM ProductEntity p ORDER BY p.pid DESC")
        Slice<Integer> findAllProductIds(Pageable pageable);

        @Query("SELECT DISTINCT p FROM ProductEntity p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.collection LEFT JOIN FETCH p.promotion WHERE p.pid IN :pids")
        List<ProductEntity> findAllByPidIn(@Param("pids") Collection<Integer> pids);

        @Query(value = """
                        SELECT p.pid
                        FROM product p
                        JOIN product_collection col ON p.collection_id = col.id
                        WHERE col.name = :collection AND p.pid <> :pid
                        ORDER BY RAND() LIMIT :limit
                        """, nativeQuery = true)
        List<Integer> findRandomIdsByCollectionAndPidNot(@Param("collection") String collection,
                        @Param("pid") Integer pid, @Param("limit") int limit);

        @Query("SELECT DISTINCT p FROM ProductEntity p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.collection WHERE p.promotion.id = :promotionId")
        List<ProductEntity> findByPromotionId(@Param("promotionId") Integer promotionId);

        @Query("SELECT DISTINCT p FROM ProductEntity p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.collection")
        List<ProductEntity> findAllWithPromotionEssentials();
}

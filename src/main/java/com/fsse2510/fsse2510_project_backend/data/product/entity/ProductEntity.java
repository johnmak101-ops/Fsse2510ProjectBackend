package com.fsse2510.fsse2510_project_backend.data.product.entity;

import com.fsse2510.fsse2510_project_backend.data.product.domainObject.ProductDetails;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "product", indexes = {
                @Index(name = "idx_product_name", columnList = "name"),
                @Index(name = "idx_product_slug", columnList = "slug", unique = true),
                @Index(name = "idx_product_category", columnList = "category_id"),
                @Index(name = "idx_product_collection", columnList = "collection_id"),
                // Composite Index for High-Performance Category Browsing
                @Index(name = "idx_product_category_pid_desc", columnList = "category_id, pid DESC"),
                // Index for Product Type Sorting
                @Index(name = "idx_product_product_type", columnList = "product_type")
})

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "pid")
        private Integer pid;

        @Column(name = "name", nullable = false)
        private String name;

        @Column(name = "slug", nullable = false, unique = true)
        private String slug;

        @Column(name = "status", length = 20)
        private String status;

        @Column(name = "description", columnDefinition = "TEXT")
        private String description;

        @Column(name = "image_url")
        private String imageUrl;

        @Column(name = "price", nullable = false)
        private BigDecimal price;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "category_id")
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        private CategoryEntity category;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "collection_id")
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        private CollectionEntity collection;

        @Column(name = "product_type")
        private String productType;

        @JdbcTypeCode(SqlTypes.JSON)
        @Column(name = "details", columnDefinition = "JSON")
        private ProductDetails details;

        @ElementCollection
        @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "pid"))
        @Column(name = "tag")
        @Builder.Default
        @BatchSize(size = 100)
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        private Set<String> tags = new HashSet<>();

        @Column(name = "is_new")
        @Builder.Default
        private Boolean isNew = false;

        @Column(name = "is_sale")
        @Builder.Default
        private Boolean isSale = false;

        @Column(name = "promotion_badge_text", length = 100)
        private String promotionBadgeText; // e.g., "20% OFF"

        @Column(name = "is_featured")
        @Builder.Default
        private Boolean isFeatured = false;

        @Column(name = "featured_priority")
        @Builder.Default
        private Integer featuredPriority = 0;

        @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
        @Builder.Default
        @BatchSize(size = 100)
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        private Set<ProductInventoryEntity> inventories = new HashSet<>();

        @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
        @Builder.Default
        @BatchSize(size = 100)
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        private Set<ProductImageEntity> images = new HashSet<>();

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "promotion_id")
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        private PromotionEntity promotion;

        @Transient
        public Integer getTotalStock() {
                return inventories.stream()
                                .mapToInt(ProductInventoryEntity::getStock)
                                .sum();
        }
}
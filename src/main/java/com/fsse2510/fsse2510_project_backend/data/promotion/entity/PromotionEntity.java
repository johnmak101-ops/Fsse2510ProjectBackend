package com.fsse2510.fsse2510_project_backend.data.promotion.entity;

import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.data.promotion.promotionType.PromotionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "promotion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@BatchSize(size = 20)
public class PromotionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PromotionType type;

    // Date Start & End
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Trigger
    private Integer minQuantity;
    private BigDecimal minAmount;
    @Enumerated(EnumType.STRING)
    @Column(name = "target_member_level")
    private MembershipLevel targetMemberLevel;

    // Target
    // If null, all SKUs apply
    @ElementCollection
    @CollectionTable(name = "promotion_target_pid", joinColumns = @JoinColumn(name = "promotion_id"))
    @Column(name = "target_pid")
    @BatchSize(size = 50)
    @Builder.Default
    private Set<Integer> targetPids = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "promotion_target_categories", joinColumns = @JoinColumn(name = "promotion_id"))
    @Column(name = "category")
    @Builder.Default
    @BatchSize(size = 50)
    private Set<String> targetCategories = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "promotion_target_collections", joinColumns = @JoinColumn(name = "promotion_id"))
    @Column(name = "collection")
    @Builder.Default
    @BatchSize(size = 50)
    private Set<String> targetCollections = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "promotion_target_tags", joinColumns = @JoinColumn(name = "promotion_id"))
    @Column(name = "tag")
    @Builder.Default
    @BatchSize(size = 50)
    private Set<String> targetTags = new HashSet<>();

    // Action
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", length = 20, columnDefinition = "VARCHAR(20)")
    private DiscountType discountType; // PERCENTAGE/FIXED
    private BigDecimal discountValue; // $$

    // Buy X Get Y
    private Integer buyX;
    private Integer getY;

    // Date Validation
    public boolean isValidDate() {
        LocalDateTime now = LocalDateTime.now();
        return (startDate == null || now.isAfter(startDate)) &&
                (endDate == null || now.isBefore(endDate));
    }
}

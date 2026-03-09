package com.fsse2510.fsse2510_project_backend.data.coupon.entity;

import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "coupon")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponEntity {
    @Id
    @Column(name = "code", nullable = false, unique = true)
    private String code; // Coupon Code (PK) e.g., "SUMMER2025"

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType; // PERCENTAGE or FIXED

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "min_spend")
    private BigDecimal minSpend; // Minimum spend

    @Column(name = "valid_until")
    private LocalDate validUntil; // Expiry date

    @Column(name = "usage_limit")
    private Integer usageLimit; // Usage limit (null for unlimited)

    @Builder.Default
    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0; // Usage count

    @Enumerated(EnumType.STRING)
    @Column(name = "required_membership_tier")
    private MembershipLevel requiredMembershipTier; // Tier restriction (null for none)

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true; // Is active
}

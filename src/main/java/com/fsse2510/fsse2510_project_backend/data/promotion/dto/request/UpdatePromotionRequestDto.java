package com.fsse2510.fsse2510_project_backend.data.promotion.dto.request;

import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.promotion.promotionType.PromotionType;
import lombok.Data;
import java.util.Set;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdatePromotionRequestDto {
    private String name;
    private String description;
    private PromotionType type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Trigger
    private Integer minQuantity;
    private BigDecimal minAmount;
    private MembershipLevel targetMemberLevel;

    // Target
    private Set<Integer> targetPids;
    private Set<String> targetCategories;
    private Set<String> targetCollections;
    private Set<String> targetTags;

    // Action
    private DiscountType discountType;
    private BigDecimal discountValue;

    // Complex
    private Integer buyX;
    private Integer getY;
}

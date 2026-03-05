package com.fsse2510.fsse2510_project_backend.data.promotion.dto.response;

import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.promotion.promotionType.PromotionType;
import lombok.Data;
import java.util.Set;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromotionResponseDto {
    private Integer id;
    private String name;
    private String description;
    private PromotionType type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer minQuantity;
    private BigDecimal minAmount;
    private MembershipLevel targetMemberLevel;
    private Set<Integer> targetPids;
    private Set<String> targetCategories;
    private Set<String> targetCollections;
    private Set<String> targetTags;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private Integer buyX;
    private Integer getY;
}

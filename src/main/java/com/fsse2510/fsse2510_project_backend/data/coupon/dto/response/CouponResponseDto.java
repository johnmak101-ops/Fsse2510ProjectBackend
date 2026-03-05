package com.fsse2510.fsse2510_project_backend.data.coupon.dto.response;

import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CouponResponseDto {
    private String code;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minSpend;
    private LocalDate validUntil;
    private Integer usageLimit;
    private Integer usageCount;
    private MembershipLevel requiredMembershipTier;
    private boolean active;
}

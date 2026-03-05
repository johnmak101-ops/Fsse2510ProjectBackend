package com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.request;

import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCouponRequestData {
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minSpend;
    private LocalDate validUntil;
    private Integer usageLimit;
    private MembershipLevel requiredMembershipTier;
    private Boolean active; // Use Boolean to allow null in update
}

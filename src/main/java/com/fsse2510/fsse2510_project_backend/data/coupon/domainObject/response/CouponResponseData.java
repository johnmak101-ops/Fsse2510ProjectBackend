package com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.response;

import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CouponResponseData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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

    // Helper: Check Validity
    public boolean isExpired() {
        return validUntil != null && LocalDate.now().isAfter(validUntil);
    }

    public boolean isUsageLimitReached() {
        return usageLimit != null && usageCount >= usageLimit;
    }

    public boolean canBeUsedBy(MembershipLevel userLevel) {
        if (requiredMembershipTier == null)
            return true;
        return userLevel.getRank() >= requiredMembershipTier.getRank();
    }
}

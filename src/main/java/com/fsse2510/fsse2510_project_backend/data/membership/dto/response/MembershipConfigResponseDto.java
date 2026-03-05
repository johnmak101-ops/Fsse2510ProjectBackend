package com.fsse2510.fsse2510_project_backend.data.membership.dto.response;

import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MembershipConfigResponseDto {
    private MembershipLevel level;
    private BigDecimal minSpend;
    private BigDecimal pointRate;
    private Integer gracePeriodDays;
}

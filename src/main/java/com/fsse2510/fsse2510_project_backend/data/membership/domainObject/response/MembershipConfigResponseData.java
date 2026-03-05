package com.fsse2510.fsse2510_project_backend.data.membership.domainObject.response;

import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
public class MembershipConfigResponseData implements Serializable {
    private static final long serialVersionUID = 1L;

    private MembershipLevel level;
    private BigDecimal minSpend;
    private BigDecimal pointRate;
    private Integer gracePeriodDays;
}

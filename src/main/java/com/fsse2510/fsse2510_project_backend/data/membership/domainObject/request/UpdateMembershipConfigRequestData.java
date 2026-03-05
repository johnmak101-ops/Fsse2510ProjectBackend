package com.fsse2510.fsse2510_project_backend.data.membership.domainObject.request;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class UpdateMembershipConfigRequestData {
    private BigDecimal minSpend;
    private BigDecimal pointRate;
    private Integer gracePeriodDays;
}

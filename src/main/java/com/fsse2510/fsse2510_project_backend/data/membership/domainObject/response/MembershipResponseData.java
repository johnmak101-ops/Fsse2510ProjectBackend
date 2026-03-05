package com.fsse2510.fsse2510_project_backend.data.membership.domainObject.response;

import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder // 建議加埋 Builder/NoArgs/AllArgs 方便 Mapper 用
@NoArgsConstructor
@AllArgsConstructor
public class MembershipResponseData implements Serializable { // [重點 2] Implements Serializable

    @Serial
    private static final long serialVersionUID = 1L; // [重點 3] Version ID

    private MembershipLevel level;
    private BigDecimal pointRate;
}
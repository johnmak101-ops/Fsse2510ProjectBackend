package com.fsse2510.fsse2510_project_backend.data.membership.dto.response;

import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MembershipResponseDto {
    // 顯示給用戶看的等級資訊
    private MembershipLevel level; // e.g. "GOLD"
    private BigDecimal pointRate;  // e.g. 0.05 (讓用戶知道自己賺分幾快)

    // 不需要顯示 minSpend 或 graceDays 給普通用戶，除非 UI 需要畫 Progress Bar
}
package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.membership.domainObject.request.UpdateMembershipConfigRequestData;
import com.fsse2510.fsse2510_project_backend.data.membership.domainObject.response.MembershipConfigResponseData;
import com.fsse2510.fsse2510_project_backend.data.membership.entity.MembershipConfigEntity;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface MembershipService {

    // Get default level (Bronze)
    MembershipLevel getDefaultLevel();

    // Get config for a specific level (Min Spend, Rate, etc.)
    MembershipConfigEntity getConfig(MembershipLevel level);

    // [State Machine] Check status and execute: Auto-renew / Enter Grace Period /
    // Downgrade
    void checkStatusAndAutoUpdate(UserEntity user);

    // [Point Logic] Calculate earned points for a transaction (including Grace
    // Period penalty logic)
    BigDecimal calculateEarnedPoints(UserEntity user, BigDecimal paidAmount);

    // [Upgrade Logic] Accumulate spending and check for upgrade
    void accumulateAndCheckUpgrade(UserEntity user, BigDecimal amount);

    List<MembershipConfigResponseData> getAllConfigs();

    @Transactional
    MembershipConfigResponseData updateConfig(MembershipLevel level, UpdateMembershipConfigRequestData requestData);
}
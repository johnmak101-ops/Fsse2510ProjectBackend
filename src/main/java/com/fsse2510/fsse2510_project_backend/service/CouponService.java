package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.request.CreateCouponRequestData;
import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.request.UpdateCouponRequestData;
import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.response.CouponResponseData;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {

    // 1. Create Coupon
    CouponResponseData createCoupon(CreateCouponRequestData requestData);

    // 2. Validate Coupon (For Transaction)
    CouponResponseData validateCoupon(String code, BigDecimal currentTotal, MembershipLevel userLevel);

    // 3. Get Valid Coupons (For Display)
    List<CouponResponseData> getValidCoupons();

    // 4. Update Coupon
    CouponResponseData updateCoupon(String code, UpdateCouponRequestData requestData);

    // 5. Delete Coupon
    void deleteCoupon(String code);

    // 6. Increment Usage
    void incrementUsage(String code);
}

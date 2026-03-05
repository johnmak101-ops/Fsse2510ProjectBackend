package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.request.CreateCouponRequestData;
import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.request.UpdateCouponRequestData;
import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.response.CouponResponseData;
import com.fsse2510.fsse2510_project_backend.data.coupon.entity.CouponEntity;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.exception.coupon.CouponAlreadyExistsException;
import com.fsse2510.fsse2510_project_backend.exception.coupon.CouponInvalidException;
import com.fsse2510.fsse2510_project_backend.mapper.coupon.CouponDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.coupon.CouponEntityMapper;
import com.fsse2510.fsse2510_project_backend.repository.CouponRepository;
import com.fsse2510.fsse2510_project_backend.service.CouponService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private static final Logger logger = LoggerFactory.getLogger(CouponServiceImpl.class);

    private final CouponRepository couponRepository;
    private final CouponEntityMapper couponEntityMapper;
    private final CouponDataMapper couponDataMapper;

    // Create Coupon
    @Override
    @Transactional
    public CouponResponseData createCoupon(CreateCouponRequestData requestData) {
        if (couponRepository.existsById(requestData.getCode())) {
            logger.warn("Create Coupon Failed: Code exists. Code={}", requestData.getCode());
            throw new CouponAlreadyExistsException(requestData.getCode());
        }

        CouponEntity entity = couponEntityMapper.toEntity(requestData);
        CouponEntity savedEntity = couponRepository.save(entity);

        return couponDataMapper.toResponseData(savedEntity);
    }

    // Validate Coupon (For Transaction)
    @Override
    public CouponResponseData validateCoupon(String code, BigDecimal currentTotal, MembershipLevel userLevel) {
        CouponEntity entity = couponRepository.findByCode(code)
                .orElseThrow(() -> {
                    logger.warn("Validate Coupon Failed: Not found. Code={}", code);
                    return new CouponInvalidException("Invalid Coupon Code");
                });

        CouponResponseData data = couponDataMapper.toResponseData(entity);

        // Check: Active Status
        if (!data.isActive()) {
            logger.warn("Validate Coupon Failed: Inactive. Code={}", code);
            throw new CouponInvalidException("Coupon is currently inactive");
        }

        // Check: Expiry
        if (data.isExpired()) {
            logger.warn("Validate Coupon Failed: Expired. Code={}, ValidUntil={}", code, data.getValidUntil());
            throw new CouponInvalidException("Coupon Expired");
        }

        // Check: Usage Limit(Feature Not Launched in Frontend)
        if (data.isUsageLimitReached()) {
            logger.warn("Validate Coupon Failed: Usage limit reached. Code={}, Count={}, Limit={}",
                    code, data.getUsageCount(), data.getUsageLimit());
            throw new CouponInvalidException("Coupon usage limit has been reached");
        }

        // Check: Membership Level(Feature Not Launched in Frontend)
        if (!data.canBeUsedBy(userLevel)) {
            logger.warn("Validate Coupon Failed: Membership tier too low. Code={}, Required={}, User={}",
                    code, data.getRequiredMembershipTier(), userLevel);
            throw new CouponInvalidException(
                    "Membership level too low to use this coupon. Required: " + data.getRequiredMembershipTier());
        }

        // Check: Min Spend
        if (data.getMinSpend() != null && currentTotal.compareTo(data.getMinSpend()) < 0) {
            logger.warn("Validate Coupon Failed: Min spend not met. Code={}, Min={}, Current={}",
                    code, data.getMinSpend(), currentTotal);
            throw new CouponInvalidException("Minimum spend requirement not met: $" + data.getMinSpend());
        }

        return data;
    }

    // Get Valid Coupons (For Display)
    @Override
    public List<CouponResponseData> getValidCoupons() {
        // Find coupons that expire after "yesterday" (valid today or in the future)
        LocalDate yesterday = LocalDate.now().minusDays(1);

        return couponRepository.findAllByValidUntilAfter(yesterday).stream()
                .map(couponDataMapper::toResponseData) // Entity -> Data
                .toList();
    }

    // Update Coupon
    @Override
    @Transactional
    public CouponResponseData updateCoupon(String code, UpdateCouponRequestData requestData) {
        CouponEntity existingCoupon = couponRepository.findByCode(code)
                .orElseThrow(() -> {
                    logger.warn("Update Coupon Failed: Not found. Code={}", code);
                    return new CouponInvalidException("Coupon not found: " + code);
                });

        couponEntityMapper.updateEntity(requestData, existingCoupon);
        CouponEntity savedEntity = couponRepository.save(existingCoupon);

        return couponDataMapper.toResponseData(savedEntity);
    }

    // Delete Coupon
    @Override
    @Transactional
    public void deleteCoupon(String code) {
        if (!couponRepository.existsById(code)) {
            logger.warn("Delete Coupon Failed: Not found. Code={}", code);
            throw new CouponInvalidException("Coupon not found: " + code);
        }
        couponRepository.deleteById(code);
    }

    @Override
    @Transactional
    public void incrementUsage(String code) {
        if (!couponRepository.existsById(code)) {
            logger.warn("Increment Usage Failed: Coupon not found. Code={}", code);
            return;
        }
        couponRepository.incrementUsageCount(code);
        logger.info("Coupon incremented usage atomically: {}", code);
    }
}
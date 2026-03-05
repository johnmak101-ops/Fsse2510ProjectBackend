package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.response.CouponResponseData;
import com.fsse2510.fsse2510_project_backend.data.coupon.entity.CouponEntity;
import com.fsse2510.fsse2510_project_backend.exception.coupon.CouponInvalidException;
import com.fsse2510.fsse2510_project_backend.mapper.coupon.CouponDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.coupon.CouponEntityMapper;
import com.fsse2510.fsse2510_project_backend.repository.CouponRepository;
import com.fsse2510.fsse2510_project_backend.service.impl.CouponServiceImpl;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;
    @Mock
    private CouponDataMapper couponDataMapper;
    @Mock
    private CouponEntityMapper couponEntityMapper;

    @InjectMocks
    private CouponServiceImpl couponService;

    @Test
    void testValidateCoupon_Success() {
        String code = "SAVE10";
        CouponEntity entity = new CouponEntity();
        CouponResponseData data = CouponResponseData.builder()
                .code(code)
                .validUntil(LocalDate.now().plusDays(1)) // Valid date
                .minSpend(new BigDecimal("100.00"))
                .active(true)
                .build();

        when(couponRepository.findByCode(code)).thenReturn(Optional.of(entity));
        when(couponDataMapper.toResponseData(entity)).thenReturn(data);

        assertDoesNotThrow(
                () -> couponService.validateCoupon(code, new BigDecimal("150.00"), MembershipLevel.NO_MEMBERSHIP));
    }

    @Test
    void testValidateCoupon_Expired() {
        String code = "EXPIRED";
        CouponEntity entity = new CouponEntity();
        CouponResponseData data = CouponResponseData.builder()
                .code(code)
                .validUntil(LocalDate.now().minusDays(1)) // Expired
                .active(true)
                .build();

        when(couponRepository.findByCode(code)).thenReturn(Optional.of(entity));
        when(couponDataMapper.toResponseData(entity)).thenReturn(data);

        assertThrows(CouponInvalidException.class,
                () -> couponService.validateCoupon(code, new BigDecimal("100.00"), MembershipLevel.NO_MEMBERSHIP));
    }

    @Test
    void testValidateCoupon_MinSpendNotMet() {
        String code = "BIGSPENDER";
        CouponEntity entity = new CouponEntity();
        CouponResponseData data = CouponResponseData.builder()
                .code(code)
                .validUntil(LocalDate.now().plusDays(1))
                .minSpend(new BigDecimal("1000.00"))
                .active(true)
                .build();

        when(couponRepository.findByCode(code)).thenReturn(Optional.of(entity));
        when(couponDataMapper.toResponseData(entity)).thenReturn(data);

        // Spend 500 < 1000
        assertThrows(CouponInvalidException.class,
                () -> couponService.validateCoupon(code, new BigDecimal("500.00"), MembershipLevel.NO_MEMBERSHIP));
    }
}

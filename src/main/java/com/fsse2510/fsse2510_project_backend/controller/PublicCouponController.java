package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.coupon.dto.response.CouponResponseDto;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.mapper.coupon.CouponDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/public/coupon")
@RequiredArgsConstructor
public class PublicCouponController {
    private final CouponService couponService;
    private final CouponDtoMapper couponDtoMapper;

    @GetMapping("/validate")
    public CouponResponseDto validateCoupon(@RequestParam String code, @RequestParam BigDecimal total) {
        return couponDtoMapper.toResponseDto(couponService.validateCoupon(code, total, MembershipLevel.NO_MEMBERSHIP));
    }
}

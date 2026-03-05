package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.coupon.dto.request.CreateCouponRequestDto;
import com.fsse2510.fsse2510_project_backend.data.coupon.dto.request.UpdateCouponRequestDto;
import com.fsse2510.fsse2510_project_backend.data.coupon.dto.response.CouponResponseDto;
import com.fsse2510.fsse2510_project_backend.mapper.coupon.CouponDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.coupon.CouponDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CouponAdminController {

    private final CouponService couponService;
    private final CouponDataMapper couponDataMapper;
    private final CouponDtoMapper couponDtoMapper;

    // 1. Create
    @PostMapping
    public CouponResponseDto createCoupon(@RequestBody @Valid CreateCouponRequestDto requestDto) {
        return couponDtoMapper.toResponseDto(
                couponService.createCoupon(
                        couponDataMapper.toCreateRequestData(requestDto)));
    }

    // 2. Get Valid Coupons (List)
    @GetMapping("/valid")
    public List<CouponResponseDto> getValidCoupons() {
        return couponService.getValidCoupons().stream()
                .map(couponDtoMapper::toResponseDto)
                .toList();
    }

    // 3. Update
    @PutMapping("/{code}")
    public CouponResponseDto updateCoupon(@PathVariable String code,
                                          @RequestBody @Valid UpdateCouponRequestDto requestDto) {
        return couponDtoMapper.toResponseDto(
                couponService.updateCoupon(
                        code,
                        couponDataMapper.toUpdateRequestData(requestDto)
                )
        );
    }

    // 4. Delete
    @DeleteMapping("/{code}")
    public void deleteCoupon(@PathVariable String code) {
        couponService.deleteCoupon(code);
    }
}

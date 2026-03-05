package com.fsse2510.fsse2510_project_backend.mapper.coupon;

import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.request.CreateCouponRequestData;
import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.response.CouponResponseData;
import com.fsse2510.fsse2510_project_backend.data.coupon.dto.request.CreateCouponRequestDto;
import com.fsse2510.fsse2510_project_backend.data.coupon.dto.response.CouponResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CouponDtoMapper {
    // Input: DTO -> Data
    CreateCouponRequestData toRequestData(CreateCouponRequestDto dto);

    // Output: Data -> DTO
    CouponResponseDto toResponseDto(CouponResponseData data);
}
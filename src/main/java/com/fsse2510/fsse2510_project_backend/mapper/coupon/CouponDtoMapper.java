package com.fsse2510.fsse2510_project_backend.mapper.coupon;

import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.request.CreateCouponRequestData;
import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.response.CouponResponseData;
import com.fsse2510.fsse2510_project_backend.data.coupon.dto.request.CreateCouponRequestDto;
import com.fsse2510.fsse2510_project_backend.data.coupon.dto.response.CouponResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CouponDtoMapper {
    @Mapping(target = "usageLimit", ignore = true)
    @Mapping(target = "requiredMembershipTier", ignore = true)
    @Mapping(target = "active", ignore = true)
    CreateCouponRequestData toRequestData(CreateCouponRequestDto dto);

    // Output: Data -> DTO
    CouponResponseDto toResponseDto(CouponResponseData data);
}
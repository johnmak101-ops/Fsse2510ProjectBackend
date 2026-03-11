package com.fsse2510.fsse2510_project_backend.mapper.coupon;

import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.response.CouponResponseData;
import com.fsse2510.fsse2510_project_backend.data.coupon.dto.response.CouponResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CouponDtoMapper {

    CouponResponseDto toResponseDto(CouponResponseData data);
}
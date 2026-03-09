package com.fsse2510.fsse2510_project_backend.mapper.coupon;

import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.request.CreateCouponRequestData;
import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.request.UpdateCouponRequestData;
import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.response.CouponResponseData;
import com.fsse2510.fsse2510_project_backend.data.coupon.dto.request.CreateCouponRequestDto;
import com.fsse2510.fsse2510_project_backend.data.coupon.dto.request.UpdateCouponRequestDto;
import com.fsse2510.fsse2510_project_backend.data.coupon.entity.CouponEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CouponDataMapper {

    @Mapping(target = "usageLimit", ignore = true)
    @Mapping(target = "requiredMembershipTier", ignore = true)
    @Mapping(target = "active", ignore = true)
    CreateCouponRequestData toCreateRequestData(CreateCouponRequestDto dto);

    @Mapping(target = "usageLimit", ignore = true)
    @Mapping(target = "requiredMembershipTier", ignore = true)
    @Mapping(target = "active", ignore = true)
    UpdateCouponRequestData toUpdateRequestData(UpdateCouponRequestDto dto);

    CouponResponseData toResponseData(CouponEntity entity);
}
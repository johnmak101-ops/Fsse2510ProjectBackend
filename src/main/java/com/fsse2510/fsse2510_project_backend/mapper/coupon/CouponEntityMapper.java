package com.fsse2510.fsse2510_project_backend.mapper.coupon;

import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.request.CreateCouponRequestData;
import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.request.UpdateCouponRequestData;
import com.fsse2510.fsse2510_project_backend.data.coupon.entity.CouponEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CouponEntityMapper {

    // Create: Data -> New Entity
    @Mapping(target = "usageCount", constant = "0")
    CouponEntity toEntity(CreateCouponRequestData data);

    // Update: Data -> Existing Entity [New]
    // Use @MappingTarget to directly update the passed entity, keeping the Service
    // clean
    @Mapping(target = "usageCount", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntity(UpdateCouponRequestData data, @MappingTarget CouponEntity entity);
}

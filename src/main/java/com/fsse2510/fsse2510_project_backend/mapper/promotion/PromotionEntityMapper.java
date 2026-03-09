package com.fsse2510.fsse2510_project_backend.mapper.promotion;

import com.fsse2510.fsse2510_project_backend.data.promotion.domainObject.request.CreatePromotionRequestData;
import com.fsse2510.fsse2510_project_backend.data.promotion.domainObject.request.UpdatePromotionRequestData;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PromotionEntityMapper {

    @Mapping(target = "id", ignore = true)
    PromotionEntity toEntity(CreatePromotionRequestData data);

    @Mapping(target = "id", ignore = true)
    void updateEntity(UpdatePromotionRequestData data, @MappingTarget PromotionEntity entity);
}
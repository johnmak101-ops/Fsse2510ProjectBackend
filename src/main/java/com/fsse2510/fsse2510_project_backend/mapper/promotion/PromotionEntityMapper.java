package com.fsse2510.fsse2510_project_backend.mapper.promotion;

import com.fsse2510.fsse2510_project_backend.data.promotion.domainObject.request.CreatePromotionRequestData;
import com.fsse2510.fsse2510_project_backend.data.promotion.domainObject.request.UpdatePromotionRequestData;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PromotionEntityMapper {

    // Create
    PromotionEntity toEntity(CreatePromotionRequestData data);

    // Update [New]
    // Use @MappingTarget to update the existing Entity from the database with Data
    // values
    void updateEntity(UpdatePromotionRequestData data, @MappingTarget PromotionEntity entity);
}
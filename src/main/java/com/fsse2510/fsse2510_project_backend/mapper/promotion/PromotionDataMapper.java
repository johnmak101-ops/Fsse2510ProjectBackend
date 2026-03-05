package com.fsse2510.fsse2510_project_backend.mapper.promotion;

import com.fsse2510.fsse2510_project_backend.data.promotion.domainObject.request.CreatePromotionRequestData;
import com.fsse2510.fsse2510_project_backend.data.promotion.domainObject.request.UpdatePromotionRequestData;
import com.fsse2510.fsse2510_project_backend.data.promotion.domainObject.response.PromotionResponseData;
import com.fsse2510.fsse2510_project_backend.data.promotion.dto.request.CreatePromotionRequestDto;
import com.fsse2510.fsse2510_project_backend.data.promotion.dto.request.UpdatePromotionRequestDto;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PromotionDataMapper {

    // Create: DTO -> Data
    @Mapping(target = "name", expression = "java(dto.getName() != null ? dto.getName().trim() : null)")
    @Mapping(target = "targetCategories", expression = "java(dto.getTargetCategories() != null ? dto.getTargetCategories().stream().map(String::trim).collect(java.util.stream.Collectors.toSet()) : null)")
    @Mapping(target = "targetCollections", expression = "java(dto.getTargetCollections() != null ? dto.getTargetCollections().stream().map(String::trim).collect(java.util.stream.Collectors.toSet()) : null)")
    @Mapping(target = "targetTags", expression = "java(dto.getTargetTags() != null ? dto.getTargetTags().stream().map(String::trim).collect(java.util.stream.Collectors.toSet()) : null)")
    CreatePromotionRequestData toRequestData(CreatePromotionRequestDto dto);

    // Update: DTO -> Data [New]
    @Mapping(target = "name", expression = "java(dto.getName() != null ? dto.getName().trim() : null)")
    @Mapping(target = "targetCategories", expression = "java(dto.getTargetCategories() != null ? dto.getTargetCategories().stream().map(String::trim).collect(java.util.stream.Collectors.toSet()) : null)")
    @Mapping(target = "targetCollections", expression = "java(dto.getTargetCollections() != null ? dto.getTargetCollections().stream().map(String::trim).collect(java.util.stream.Collectors.toSet()) : null)")
    @Mapping(target = "targetTags", expression = "java(dto.getTargetTags() != null ? dto.getTargetTags().stream().map(String::trim).collect(java.util.stream.Collectors.toSet()) : null)")
    UpdatePromotionRequestData toUpdateRequestData(UpdatePromotionRequestDto dto);

    // Response: Entity -> Data
    PromotionResponseData toResponseData(PromotionEntity entity);
}
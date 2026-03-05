package com.fsse2510.fsse2510_project_backend.mapper.promotion;

import com.fsse2510.fsse2510_project_backend.data.promotion.domainObject.response.PromotionResponseData;
import com.fsse2510.fsse2510_project_backend.data.promotion.dto.response.PromotionResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PromotionDtoMapper {

    // Service Output (Data) -> Controller Output (DTO)
    PromotionResponseDto toResponseDto(PromotionResponseData data);
}

package com.fsse2510.fsse2510_project_backend.mapper.showcase;

import com.fsse2510.fsse2510_project_backend.data.showcase.domainObject.ShowcaseCollectionAdminData;
import com.fsse2510.fsse2510_project_backend.data.showcase.dto.response.ShowcaseCollectionAdminResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShowcaseDtoMapper {

    ShowcaseCollectionAdminResponseDto toResponseDto(ShowcaseCollectionAdminData data);
}

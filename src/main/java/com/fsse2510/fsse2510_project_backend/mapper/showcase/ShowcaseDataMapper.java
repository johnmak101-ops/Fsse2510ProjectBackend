package com.fsse2510.fsse2510_project_backend.mapper.showcase;

import com.fsse2510.fsse2510_project_backend.data.showcase.domainObject.ShowcaseCollectionAdminData;
import com.fsse2510.fsse2510_project_backend.data.showcase.dto.request.CreateShowcaseCollectionRequestDto;
import com.fsse2510.fsse2510_project_backend.data.showcase.dto.request.UpdateShowcaseCollectionRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShowcaseDataMapper {

    @Mapping(target = "id", ignore = true)
    ShowcaseCollectionAdminData toAdminData(CreateShowcaseCollectionRequestDto dto);

    @Mapping(target = "id", ignore = true)
    ShowcaseCollectionAdminData toAdminData(UpdateShowcaseCollectionRequestDto dto);
}

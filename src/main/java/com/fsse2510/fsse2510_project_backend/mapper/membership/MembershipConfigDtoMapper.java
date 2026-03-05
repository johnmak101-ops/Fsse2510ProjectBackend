package com.fsse2510.fsse2510_project_backend.mapper.membership;

import com.fsse2510.fsse2510_project_backend.data.membership.domainObject.response.MembershipConfigResponseData;
import com.fsse2510.fsse2510_project_backend.data.membership.dto.response.MembershipConfigResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MembershipConfigDtoMapper {

    // Data -> Response DTO
    MembershipConfigResponseDto toResponseDto(MembershipConfigResponseData data);
}

package com.fsse2510.fsse2510_project_backend.mapper.membership;

import com.fsse2510.fsse2510_project_backend.data.membership.domainObject.response.MembershipResponseData;
import com.fsse2510.fsse2510_project_backend.data.membership.dto.response.MembershipResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MembershipDtoMapper {

    // Data -> Response DTO (User Profile 裡面的 Membership 區塊)
    MembershipResponseDto toResponseDto(MembershipResponseData data);
}
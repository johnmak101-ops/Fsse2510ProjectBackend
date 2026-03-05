package com.fsse2510.fsse2510_project_backend.mapper.user;

import com.fsse2510.fsse2510_project_backend.data.user.domainObject.response.UserData;
import com.fsse2510.fsse2510_project_backend.data.user.dto.response.UserResponseDto;
import com.fsse2510.fsse2510_project_backend.mapper.membership.MembershipDtoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MembershipDtoMapper.class})
public interface UserDtoMapper {

    @Mapping(target = "membership", source = "membership")
    UserResponseDto toResponseDto(UserData data);
}
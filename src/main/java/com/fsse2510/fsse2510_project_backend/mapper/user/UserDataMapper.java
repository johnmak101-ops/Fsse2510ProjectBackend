package com.fsse2510.fsse2510_project_backend.mapper.user;

import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.UpdateUserProfileRequestData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.response.UserData;
import com.fsse2510.fsse2510_project_backend.data.user.dto.request.UpdateUserProfileRequestDto;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserDataMapper {

    UpdateUserProfileRequestData toUpdateData(UpdateUserProfileRequestDto dto);

    void updateEntity(UpdateUserProfileRequestData data, @MappingTarget UserEntity entity);

    @Mapping(target = "membership", ignore = true)
    @Mapping(target = "role", ignore = true)
    UserData toDomain(UserEntity entity);

    @Mapping(target = "uid", ignore = true)
    @Mapping(target = "firebaseUid", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "level", ignore = true)
    void updateEntity(UserData data, @MappingTarget UserEntity entity);
}
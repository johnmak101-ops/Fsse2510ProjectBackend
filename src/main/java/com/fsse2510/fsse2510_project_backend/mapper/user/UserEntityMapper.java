package com.fsse2510.fsse2510_project_backend.mapper.user;

import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    @Mapping(target = "uid", ignore = true)
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "accumulatedSpending", ignore = true)
    @Mapping(target = "cycleSpending", ignore = true)
    @Mapping(target = "points", ignore = true)
    @Mapping(target = "cycleEndDate", ignore = true)
    @Mapping(target = "isInGracePeriod", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "birthday", ignore = true)
    UserEntity toEntity(FirebaseUserData data);
}

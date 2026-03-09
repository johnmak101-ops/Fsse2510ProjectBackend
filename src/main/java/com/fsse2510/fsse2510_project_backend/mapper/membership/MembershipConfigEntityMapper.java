package com.fsse2510.fsse2510_project_backend.mapper.membership;

import com.fsse2510.fsse2510_project_backend.data.membership.domainObject.request.UpdateMembershipConfigRequestData;
import com.fsse2510.fsse2510_project_backend.data.membership.entity.MembershipConfigEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MembershipConfigEntityMapper {

    @Mapping(target = "level", ignore = true)
    void updateEntity(UpdateMembershipConfigRequestData data, @MappingTarget MembershipConfigEntity entity);
}

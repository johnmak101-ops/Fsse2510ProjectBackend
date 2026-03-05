package com.fsse2510.fsse2510_project_backend.mapper.membership;

import com.fsse2510.fsse2510_project_backend.data.membership.domainObject.request.UpdateMembershipConfigRequestData;
import com.fsse2510.fsse2510_project_backend.data.membership.entity.MembershipConfigEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MembershipEntityMapper {
    // Admin Update Config: Data -> Entity
    void updateEntity(UpdateMembershipConfigRequestData data, @MappingTarget MembershipConfigEntity entity);
}

package com.fsse2510.fsse2510_project_backend.mapper.membership;


import com.fsse2510.fsse2510_project_backend.data.membership.domainObject.response.MembershipResponseData;
import com.fsse2510.fsse2510_project_backend.data.membership.entity.MembershipConfigEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MembershipDataMapper {
    MembershipResponseData toResponseData(MembershipConfigEntity entity);
}

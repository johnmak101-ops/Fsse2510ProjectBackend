package com.fsse2510.fsse2510_project_backend.mapper.membership;

import com.fsse2510.fsse2510_project_backend.data.membership.domainObject.request.UpdateMembershipConfigRequestData;
import com.fsse2510.fsse2510_project_backend.data.membership.domainObject.response.MembershipConfigResponseData;
import com.fsse2510.fsse2510_project_backend.data.membership.dto.request.UpdateMembershipConfigRequestDto;
import com.fsse2510.fsse2510_project_backend.data.membership.entity.MembershipConfigEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MembershipConfigDataMapper {

    // DTO -> Request Data
    UpdateMembershipConfigRequestData toRequestData(UpdateMembershipConfigRequestDto dto);

    // Entity -> Response Data
    MembershipConfigResponseData toResponseData(MembershipConfigEntity entity);
}

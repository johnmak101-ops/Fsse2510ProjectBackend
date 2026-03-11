package com.fsse2510.fsse2510_project_backend.data.user.domainObject.request;

import com.fsse2510.fsse2510_project_backend.data.user.dto.request.SetUserRoleRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetUserRoleRequestData {
    private String uid;
    private String role;

    public SetUserRoleRequestData(SetUserRoleRequestDto dto) {
        this.uid = dto.getUid();
        this.role = dto.getRole();
    }
}

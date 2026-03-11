package com.fsse2510.fsse2510_project_backend.data.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.response.AdminUserResponseData;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminUserResponseDto {
    private String uid;
    private String email;
    @JsonProperty("isAdmin")
    private boolean isAdmin;

    public AdminUserResponseDto(AdminUserResponseData data) {
        this.uid = data.getUid();
        this.email = data.getEmail();
        this.isAdmin = data.isAdmin();
    }
}

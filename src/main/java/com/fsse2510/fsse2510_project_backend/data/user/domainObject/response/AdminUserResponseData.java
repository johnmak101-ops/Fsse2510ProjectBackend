package com.fsse2510.fsse2510_project_backend.data.user.domainObject.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponseData implements Serializable {
    private String uid;
    private String email;
    private boolean isAdmin;
}

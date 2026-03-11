package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.SetUserRoleRequestData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.response.AdminUserResponseData;

import java.util.List;

public interface AdminUserService {
    List<AdminUserResponseData> getAllUsers();

    boolean setUserRole(SetUserRoleRequestData data);

    List<AdminUserResponseData> searchUsers(String query);
}

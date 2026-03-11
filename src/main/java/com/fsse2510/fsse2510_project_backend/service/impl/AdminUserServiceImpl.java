package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.SetUserRoleRequestData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.response.AdminUserResponseData;
import com.fsse2510.fsse2510_project_backend.service.AdminUserService;
import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.ListUsersPage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Override
    public boolean setUserRole(SetUserRoleRequestData data) {
        try {
            Map<String, Object> claims = new HashMap<>();

            if ("ADMIN".equalsIgnoreCase(data.getRole())) {
                claims.put("admin", true);
            } else if ("CUSTOMER".equalsIgnoreCase(data.getRole())) {
                claims.put("admin", false);
                claims.put("customer", true);
            } else {
                throw new IllegalArgumentException("Unknown role. Use ADMIN or CUSTOMER.");
            }

            FirebaseAuth.getInstance().setCustomUserClaims(data.getUid(), claims);
            return true;
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Failed to set user role in Firebase.", e);
        }
    }

    @Override
    public List<AdminUserResponseData> getAllUsers() {
        try {
            List<AdminUserResponseData> users = new ArrayList<>();
            ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
            for (ExportedUserRecord user : page.iterateAll()) {
                users.add(mapToAdminUserResponseData(user));
            }
            return users;
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Failed to fetch users from Firebase.", e);
        }
    }

    @Override
    public List<AdminUserResponseData> searchUsers(String query) {
        try {
            List<AdminUserResponseData> matchingUsers = new ArrayList<>();
            ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);

            String lowerQuery = query.toLowerCase();

            for (ExportedUserRecord user : page.iterateAll()) {
                boolean matchesEmail = user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery);
                boolean matchesUid = user.getUid().toLowerCase().contains(lowerQuery);

                if (matchesEmail || matchesUid) {
                    matchingUsers.add(mapToAdminUserResponseData(user));
                }
            }
            return matchingUsers;
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Failed to search users from Firebase.", e);
        }
    }

    private AdminUserResponseData mapToAdminUserResponseData(ExportedUserRecord user) {
        Map<String, Object> claims = user.getCustomClaims();
        boolean isAdmin = claims != null && Boolean.TRUE.equals(claims.get("admin"));

        return AdminUserResponseData.builder()
                .uid(user.getUid())
                .email(user.getEmail())
                .isAdmin(isAdmin)
                .build();
    }
}

package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.user.dto.response.AdminUserResponseDto;
import com.fsse2510.fsse2510_project_backend.data.user.dto.request.SetUserRoleRequestDto;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.SetUserRoleRequestData;
import com.fsse2510.fsse2510_project_backend.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final AdminUserService adminUserService;

    @GetMapping
    public List<AdminUserResponseDto> getAllUsers() {
        return adminUserService.getAllUsers().stream()
                .map(AdminUserResponseDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<AdminUserResponseDto> searchUsers(@RequestParam String query) {
        return adminUserService.searchUsers(query).stream()
                .map(AdminUserResponseDto::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/set-role")
    public String setUserRole(@RequestBody @Valid SetUserRoleRequestDto dto) {
        adminUserService.setUserRole(new SetUserRoleRequestData(dto));
        return "Successfully set role [" + dto.getRole().toUpperCase() + "] for UID: " + dto.getUid()
                + " (User needs to re-login to get the new token)";
    }
}

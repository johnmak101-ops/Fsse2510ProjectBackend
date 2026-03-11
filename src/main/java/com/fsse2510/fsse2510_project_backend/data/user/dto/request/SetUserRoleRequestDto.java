package com.fsse2510.fsse2510_project_backend.data.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetUserRoleRequestDto {
    @NotBlank
    private String uid;
    @NotBlank
    private String role;
}

package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.membership.dto.request.UpdateMembershipConfigRequestDto;
import com.fsse2510.fsse2510_project_backend.data.membership.dto.response.MembershipConfigResponseDto;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.mapper.membership.MembershipConfigDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.membership.MembershipConfigDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/membership/configs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MembershipAdminController {
    private final MembershipService membershipService;
    private final MembershipConfigDataMapper configDataMapper;
    private final MembershipConfigDtoMapper configDtoMapper;

    //Membership Tier Control

    @GetMapping
    public List<MembershipConfigResponseDto> getAllConfigs() {
        return membershipService.getAllConfigs().stream()
                .map(configDtoMapper::toResponseDto)
                .toList();
    }

    @PutMapping("/{level}")
    public MembershipConfigResponseDto updateConfig(@PathVariable MembershipLevel level,
                                                    @RequestBody @Valid UpdateMembershipConfigRequestDto requestDto) {
        return configDtoMapper.toResponseDto(
                membershipService.updateConfig(level, configDataMapper.toRequestData(requestDto)
                )
        );
    }
}
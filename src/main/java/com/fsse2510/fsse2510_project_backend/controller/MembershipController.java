package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.membership.dto.response.MembershipConfigResponseDto;
import com.fsse2510.fsse2510_project_backend.mapper.membership.MembershipConfigDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/membership")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;
    private final MembershipConfigDtoMapper membershipConfigDtoMapper;

    @GetMapping("/tiers")
    public List<MembershipConfigResponseDto> getAllMembershipTiers() {
        return membershipService.getAllConfigs().stream()
                .map(membershipConfigDtoMapper::toResponseDto)
                .toList();
    }
}

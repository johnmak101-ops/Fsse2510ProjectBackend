package com.fsse2510.fsse2510_project_backend.data.user.dto.response;

import com.fsse2510.fsse2510_project_backend.data.membership.dto.response.MembershipResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UserResponseDto {
    private Integer uid;
    private String email;
    private String firebaseUid;

    // --- Membership Info ---
    private MembershipResponseDto membership; // 內嵌 Membership DTO

    private BigDecimal accumulatedSpending;
    private BigDecimal cycleSpending;
    private BigDecimal points;
    private LocalDate cycleEndDate;
    private Boolean isInGracePeriod;

    // --- Profile Info ---
    private String fullName;
    private String phoneNumber;
    private String address;
    private LocalDate birthday;

    // --- Frontend Flag ---
    @JsonProperty("isInfoComplete")
    private boolean isInfoComplete; // true/false
}

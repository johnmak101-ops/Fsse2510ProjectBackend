package com.fsse2510.fsse2510_project_backend.data.user.domainObject.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fsse2510.fsse2510_project_backend.data.membership.domainObject.response.MembershipResponseData;
import com.fsse2510.fsse2510_project_backend.data.user.UserRole;
import com.fsse2510.fsse2510_project_backend.util.ValidationUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer uid;
    private String email;
    private String firebaseUid;

    /** The resolved application role for this user (ADMIN or USER). */
    private UserRole role;

    // Membership Info
    private MembershipResponseData membership; // Membership Data Object

    private BigDecimal accumulatedSpending;
    private BigDecimal cycleSpending;
    private BigDecimal points;
    private LocalDate cycleEndDate; // Member end date
    private Boolean isInGracePeriod; // Member grace period

    // Profile Info
    private String fullName;
    private String phoneNumber;
    private String address;
    private LocalDate birthday;

    // Logic Helper
    // Logic to validate the profile is completed
    // If completed, non-member can upgrade to member
    @JsonIgnore
    public boolean isInfoComplete() {
        return ValidationUtil.isProfileComplete(fullName, phoneNumber);
    }
}

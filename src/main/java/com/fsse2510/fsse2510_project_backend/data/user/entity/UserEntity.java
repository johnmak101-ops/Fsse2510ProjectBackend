package com.fsse2510.fsse2510_project_backend.data.user.entity;

import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.fsse2510.fsse2510_project_backend.util.ValidationUtil;

@Entity
@Table(name = "user", indexes = {
        @Index(name = "idx_user_firebase_uid", columnList = "firebase_uid", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer uid;

    @Column(nullable = false)
    private String email;

    @Column(name = "firebase_uid", nullable = false, unique = true)
    private String firebaseUid;

    // --- Membership Fields ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MembershipLevel level = MembershipLevel.NO_MEMBERSHIP;

    @Column(name = "accumulated_spending")
    @Builder.Default
    private BigDecimal accumulatedSpending = BigDecimal.ZERO;

    @Column(name = "cycle_spending")
    @Builder.Default
    private BigDecimal cycleSpending = BigDecimal.ZERO;

    @Column(name = "points")
    @Builder.Default
    private BigDecimal points = BigDecimal.ZERO;

    @Column(name = "cycle_end_date")
    private LocalDate cycleEndDate;

    @Column(name = "is_in_grace_period")
    @Builder.Default
    private Boolean isInGracePeriod = false;

    // --- Profile Info ---
    private String fullName;
    private String phoneNumber;
    private String address;
    private LocalDate birthday;

    public boolean isInfoComplete() {
        return ValidationUtil.isProfileComplete(fullName, phoneNumber);
    }
}
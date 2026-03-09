package com.fsse2510.fsse2510_project_backend.data.membership.entity;

import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "membership_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipConfigEntity {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private MembershipLevel level;

    @Column(name = "min_spend", nullable = false)
    private BigDecimal minSpend;

    @Column(name = "point_rate", nullable = false)
    private BigDecimal pointRate;

    @Column(name = "grace_period_days", nullable = false)
    private Integer gracePeriodDays;
}
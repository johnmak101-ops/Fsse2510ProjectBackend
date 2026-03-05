package com.fsse2510.fsse2510_project_backend.repository;

import com.fsse2510.fsse2510_project_backend.data.membership.entity.MembershipConfigEntity;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipConfigRepository extends JpaRepository<MembershipConfigEntity, MembershipLevel> {
}

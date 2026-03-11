package com.fsse2510.fsse2510_project_backend.repository;

import com.fsse2510.fsse2510_project_backend.data.coupon.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.time.LocalDate;
import java.util.List;

public interface CouponRepository extends JpaRepository<CouponEntity, String> {
    Optional<CouponEntity> findByCode(String code);

    // Find coupons that expire after today
    List<CouponEntity> findAllByValidUntilAfter(LocalDate date);

    @Modifying
    @Query("UPDATE CouponEntity c SET c.usageCount = c.usageCount + 1 " +
            "WHERE c.code = :code AND (c.usageLimit IS NULL OR c.usageCount < c.usageLimit)")
    int incrementUsageCount(@Param("code") String code);
}

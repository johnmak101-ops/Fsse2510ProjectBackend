package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.coupon.dto.response.CouponResponseDto;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.response.UserData;
import com.fsse2510.fsse2510_project_backend.mapper.coupon.CouponDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.CouponService;
import com.fsse2510.fsse2510_project_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/public/coupon")
@RequiredArgsConstructor
public class PublicCouponController {

    private static final Logger logger = LoggerFactory.getLogger(PublicCouponController.class);

    private final CouponService couponService;
    private final CouponDtoMapper couponDtoMapper;
    private final UserService userService;

    @GetMapping("/validate")
    public CouponResponseDto validateCoupon(@RequestParam String code,
                                            @RequestParam BigDecimal total,
                                            JwtAuthenticationToken token) {
        MembershipLevel level = resolveMembershipLevel(token);
        return couponDtoMapper.toResponseDto(couponService.validateCoupon(code, total, level));
    }

    private MembershipLevel resolveMembershipLevel(JwtAuthenticationToken token) {
        if (token == null) {
            return MembershipLevel.NO_MEMBERSHIP;
        }
        try {
            FirebaseUserData firebaseUser = FirebaseUserData.builder()
                    .firebaseUid(token.getToken().getSubject())
                    .email((String) token.getTokenAttributes().get("email"))
                    .build();
            UserData userData = userService.getOrCreateUser(firebaseUser);
            return userData.getMembership() != null
                    ? userData.getMembership().getLevel()
                    : MembershipLevel.NO_MEMBERSHIP;
        } catch (Exception e) {
            logger.debug("Could not resolve membership level from token, defaulting to NO_MEMBERSHIP", e);
            return MembershipLevel.NO_MEMBERSHIP;
        }
    }
}

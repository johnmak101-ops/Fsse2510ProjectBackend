package com.fsse2510.fsse2510_project_backend.exception.coupon;

public class CouponAlreadyExistsException extends RuntimeException {
    public CouponAlreadyExistsException(String code) {
        super("Coupon code already exists: " + code);
    }
}

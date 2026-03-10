package com.fsse2510.fsse2510_project_backend.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BusinessConstants {
    /**
     * Standard rounding mode for all monetary calculations
     */
    public static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    /**
     * Rounding mode for points deduction (separate from money for independent tuning)
     */
    public static final RoundingMode POINTS_ROUNDING = RoundingMode.HALF_UP;

    /**
     * Point conversion rate: 10 points = $1.00 HKD (Standard 1% rate = $10 spend :
     * 1 point)
     */
    public static final int POINTS_TO_DOLLAR_RATE = 10;

    /**
     * Default point rate floor for grace period (0.5%)
     */
    public static final BigDecimal GRACE_PERIOD_FLOOR_RATE = new BigDecimal("0.005");

    /**
     * Standard money scale for BigDecimal rounding
     */
    public static final int MONEY_SCALE = 2;

    /**
     * Minimum payment amount allowed ($4.00)
     */
    public static final BigDecimal MIN_PAYMENT_AMOUNT = new BigDecimal("4.00");

    /**
     * Stripe Bypass Session ID for zero-total transactions
     */
    public static final String STRIPE_BYPASS_SESSION_ID = "skip_stripe";
}

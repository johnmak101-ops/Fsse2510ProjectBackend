package com.fsse2510.fsse2510_project_backend.data.coupon.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateCouponRequestDto {

    private String description;

    @Pattern(regexp = "^(?i)(PERCENTAGE|FIXED)$", message = "Discount type must be PERCENTAGE or FIXED")
    private String discountType;

    @Positive(message = "Discount value must be positive")
    private BigDecimal discountValue;

    @PositiveOrZero(message = "Min spend must be zero or positive")
    private BigDecimal minSpend;

    @Future(message = "Expiry date must be in the future")
    private LocalDate validUntil;
}

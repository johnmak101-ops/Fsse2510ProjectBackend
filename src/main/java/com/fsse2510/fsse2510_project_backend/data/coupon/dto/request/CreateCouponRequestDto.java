package com.fsse2510.fsse2510_project_backend.data.coupon.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCouponRequestDto {

    @NotBlank(message = "Coupon code cannot be empty")
    @Size(min = 3, max = 20, message = "Coupon code length must be between 3 and 20")
    @JsonProperty("code")
    private String code;

    @JsonProperty("description")
    private String description;

    @Pattern(regexp = "^(?i)(PERCENTAGE|FIXED)$", message = "Discount type must be PERCENTAGE or FIXED")
    @NotBlank(message = "Discount type is required")
    @JsonProperty("discountType")
    private String discountType;

    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be positive")
    @JsonProperty("discountValue")
    private BigDecimal discountValue;

    @PositiveOrZero(message = "Min spend must be zero or positive")
    @JsonProperty("minSpend")
    private BigDecimal minSpend;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    @JsonProperty("validUntil")
    private LocalDate validUntil;
}

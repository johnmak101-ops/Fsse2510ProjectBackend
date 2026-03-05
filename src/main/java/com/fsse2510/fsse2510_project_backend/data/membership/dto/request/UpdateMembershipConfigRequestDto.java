package com.fsse2510.fsse2510_project_backend.data.membership.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateMembershipConfigRequestDto {
    // Level 通常由 PathVariable 傳入，這裡只放可修改的數值

    @NotNull(message = "Min spend is required")
    @PositiveOrZero(message = "Min spend must be zero or positive")
    private BigDecimal minSpend;

    @NotNull(message = "Point rate is required")
    @PositiveOrZero(message = "Point rate must be zero or positive")
    private BigDecimal pointRate;

    @NotNull(message = "Grace period days is required")
    @Min(value = 0, message = "Grace period days must be zero or positive")
    private Integer gracePeriodDays;
}
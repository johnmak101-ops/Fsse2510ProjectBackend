package com.fsse2510.fsse2510_project_backend.data.promotion.dto.request;

import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.promotion.promotionType.PromotionType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.util.Set;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreatePromotionRequestDto {
    @NotBlank(message = "Promotion name cannot be empty")
    private String name;

    private String description;

    @NotNull(message = "Promotion type is required")
    private PromotionType type;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

    @PositiveOrZero(message = "Min quantity must be zero or positive")
    private Integer minQuantity;

    @PositiveOrZero(message = "Min amount must be zero or positive")
    private BigDecimal minAmount;

    private MembershipLevel targetMemberLevel;

    private Set<Integer> targetPids;
    private Set<String> targetCategories;
    private Set<String> targetCollections;
    private Set<String> targetTags;

    private DiscountType discountType;

    @Positive(message = "Discount value must be positive")
    private BigDecimal discountValue;

    private Integer buyX;

    private Integer getY;
}

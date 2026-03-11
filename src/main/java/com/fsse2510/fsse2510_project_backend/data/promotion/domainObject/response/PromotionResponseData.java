package com.fsse2510.fsse2510_project_backend.data.promotion.domainObject.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.promotion.promotionType.PromotionType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class PromotionResponseData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private String description;
    private PromotionType type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer minQuantity;
    private BigDecimal minAmount;
    private com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel targetMemberLevel;
    private Set<Integer> targetPids;
    private Set<String> targetCategories;
    private Set<String> targetCollections;
    private Set<String> targetTags;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private Integer buyX;
    private Integer getY;

    // Helper for Logic check
    @JsonIgnore
    public boolean isValidDate() {
        LocalDateTime now = LocalDateTime.now();
        return (startDate == null || now.isAfter(startDate)) &&
                (endDate == null || now.isBefore(endDate));
    }
}
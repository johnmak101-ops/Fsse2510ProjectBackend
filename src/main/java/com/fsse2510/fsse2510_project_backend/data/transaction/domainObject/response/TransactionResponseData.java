package com.fsse2510.fsse2510_project_backend.data.transaction.domainObject.response;

import com.fsse2510.fsse2510_project_backend.data.transaction.status.PaymentStatus;
import com.fsse2510.fsse2510_project_backend.data.transactionProduct.domainObject.response.TransactionProductResponseData;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer tid;
    private Integer buyerUid;
    private LocalDateTime datetime;
    private PaymentStatus status;
    private BigDecimal total;
    private List<TransactionProductResponseData> items;
    private Integer usedPoints;
    private String couponCode;
    private BigDecimal earnedPoints;

    // Shipping Address info
    private String recipientName;
    private String phoneNumber;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateProvince;
    private String postalCode;

    private com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel previousLevel;
    private com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel newLevel;
}
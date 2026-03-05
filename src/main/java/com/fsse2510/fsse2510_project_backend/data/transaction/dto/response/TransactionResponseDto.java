package com.fsse2510.fsse2510_project_backend.data.transaction.dto.response;

import com.fsse2510.fsse2510_project_backend.data.transaction.status.PaymentStatus;
import com.fsse2510.fsse2510_project_backend.data.transactionProduct.dto.response.TransactionProductResponseDto;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {
    private Integer tid;

    private Integer buyerUid;

    private LocalDateTime datetime;
    private PaymentStatus status;
    private BigDecimal total;
    private List<TransactionProductResponseDto> items;
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
}

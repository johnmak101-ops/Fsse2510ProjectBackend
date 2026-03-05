package com.fsse2510.fsse2510_project_backend.data.payment.domainObject.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PaymentResponseData {
    private String clientSecret;
    private Integer transactionId;
    private BigDecimal amount;
    private String url;
}

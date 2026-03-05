package com.fsse2510.fsse2510_project_backend.data.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
public class PaymentResponseDto {
    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("tid")
    private Integer tid;

    private BigDecimal amount;
    private String url;
}

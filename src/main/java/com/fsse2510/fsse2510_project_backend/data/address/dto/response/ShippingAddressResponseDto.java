package com.fsse2510.fsse2510_project_backend.data.address.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShippingAddressResponseDto {
    private Integer id;
    private String recipientName;
    private String phoneNumber;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateProvince;
    private String postalCode;
    private Boolean isDefault;
}

package com.fsse2510.fsse2510_project_backend.data.address.domainObject.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressResponseData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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

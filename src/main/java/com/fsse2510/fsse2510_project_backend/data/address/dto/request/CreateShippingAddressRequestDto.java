package com.fsse2510.fsse2510_project_backend.data.address.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateShippingAddressRequestDto {
    @NotBlank(message = "Recipient name is required")
    @Size(min = 2, max = 100, message = "Recipient name must be between 2 and 100 characters")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Recipient name must only contain letters and spaces")
    private String recipientName;

    @NotBlank(message = "Phone number is required")
    @Size(min = 8, max = 30, message = "Phone number must be at least 8 characters")
    @Pattern(regexp = "^\\+?[0-9\\-\\s\\(\\)]{6,28}[0-9]$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    private String stateProvince;

    private String postalCode;

    private Boolean isDefault = false;
}

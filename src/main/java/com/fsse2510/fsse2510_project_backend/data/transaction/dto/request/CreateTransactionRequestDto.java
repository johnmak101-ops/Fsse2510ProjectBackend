package com.fsse2510.fsse2510_project_backend.data.transaction.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CreateTransactionRequestDto {
    private String couponCode; // Optional (e.g. "SUMMER2025")
    @Min(value = 0, message = "Points must be greater than or equal to 0")
    private Integer usePoints; // Optional (e.g. 100)

    private Integer addressId; // Optional for now, but will be required in UI
}
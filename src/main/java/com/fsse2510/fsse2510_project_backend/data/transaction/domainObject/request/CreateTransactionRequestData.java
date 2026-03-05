package com.fsse2510.fsse2510_project_backend.data.transaction.domainObject.request;

import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequestData {
    private FirebaseUserData user; // 從 Token 黎
    private String couponCode; // 從 DTO 黎
    private Integer usePoints; // 從 DTO 黎
    private Integer addressId; // 從 DTO 黎
}
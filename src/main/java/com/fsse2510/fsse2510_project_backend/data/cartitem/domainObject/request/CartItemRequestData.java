package com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.request;

import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
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
public class CartItemRequestData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String sku; // Use SKU instead of PID for variant selection
    private Integer quantity;
    private FirebaseUserData user;
}
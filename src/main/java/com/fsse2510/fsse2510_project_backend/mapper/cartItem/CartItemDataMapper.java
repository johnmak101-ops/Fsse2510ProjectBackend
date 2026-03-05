package com.fsse2510.fsse2510_project_backend.mapper.cartItem;

import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.request.CartItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartItemDataMapper {

    default CartItemRequestData toRequestData(String sku, Integer quantity, FirebaseUserData user) {
        return CartItemRequestData.builder()
                .sku(sku)
                .quantity(quantity)
                .user(user)
                .build();
    }
}

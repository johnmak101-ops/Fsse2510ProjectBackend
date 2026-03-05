package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.request.CartItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.response.CartItemResponseData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;

import java.util.List;

public interface CartItemService {
    List<CartItemResponseData> addCartItem(CartItemRequestData requestData);

    List<CartItemResponseData> addCartItems(List<CartItemRequestData> requestDataList);

    List<CartItemResponseData> getUserCart(FirebaseUserData firebaseUser);

    List<CartItemResponseData> updateCartItemQuantity(CartItemRequestData requestData);

    List<CartItemResponseData> removeCartItem(CartItemRequestData requestData);

    void clearCart(UserEntity userRef);
}
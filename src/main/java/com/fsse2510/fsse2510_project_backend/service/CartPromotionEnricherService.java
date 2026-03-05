package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.response.CartItemResponseData;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;

import java.util.List;

public interface CartPromotionEnricherService {

    List<CartItemResponseData> enrichWithPromotions(List<CartItemResponseData> cartItems,
            UserEntity user);

    void clearCache();
}

package com.fsse2510.fsse2510_project_backend.mapper.cartItem;

import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.request.CartItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.response.CartItemResponseData;
import com.fsse2510.fsse2510_project_backend.data.cartitem.entity.CartItemEntity;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemDataMapper {

    default CartItemRequestData toRequestData(String sku, Integer quantity, FirebaseUserData user) {
        return CartItemRequestData.builder()
                .sku(sku)
                .quantity(quantity)
                .user(user)
                .build();
    }

    // Response: Entity -> ResponseData (Flatten Product details from variant)
    @Mapping(target = "pid", source = "productInventory.product.pid")
    @Mapping(target = "slug", source = "productInventory.product.slug")
    @Mapping(target = "sku", source = "productInventory.sku")
    @Mapping(target = "name", source = "productInventory.product.name")
    @Mapping(target = "imageUrl", source = "productInventory.product.imageUrl")
    @Mapping(target = "price", source = "productInventory.product.price")
    @Mapping(target = "stock", source = "productInventory.stock")
    @Mapping(target = "cartQuantity", source = "quantity")
    @Mapping(target = "selectedSize", source = "productInventory.size")
    @Mapping(target = "selectedColor", source = "productInventory.color")
    @Mapping(target = "category", source = "productInventory.product.category.name")
    @Mapping(target = "collection", source = "productInventory.product.collection.name")
    @Mapping(target = "tags", source = "productInventory.product.tags")
    @Mapping(target = "originalPrice", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "discountPercentage", ignore = true)
    @Mapping(target = "promotionBadgeTexts", ignore = true)
    @Mapping(target = "appliedPromotionIds", ignore = true)
    CartItemResponseData toResponseData(CartItemEntity entity);
}

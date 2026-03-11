package com.fsse2510.fsse2510_project_backend.mapper.cartItem;

import com.fsse2510.fsse2510_project_backend.data.cartitem.entity.CartItemEntity;
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductInventoryEntity;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemEntityMapper {

    // Create: Use Request Data quantity along with User and Product Inventory to
    // create CartItem
    @Mapping(target = "cid", ignore = true)
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "productInventory", source = "inventory")
    CartItemEntity toEntity(Integer quantity, UserEntity user, ProductInventoryEntity inventory);
}
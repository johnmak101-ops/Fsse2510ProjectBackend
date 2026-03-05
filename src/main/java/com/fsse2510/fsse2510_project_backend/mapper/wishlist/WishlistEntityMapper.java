package com.fsse2510.fsse2510_project_backend.mapper.wishlist;

import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import com.fsse2510.fsse2510_project_backend.data.wishlist.domainObject.response.WishlistResponseData;
import com.fsse2510.fsse2510_project_backend.data.wishlist.entity.WishlistEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WishlistEntityMapper {

    @Mapping(target = "wid", ignore = true)
    @Mapping(target = "product", source = "product")
    @Mapping(target = "user", source = "user")
    WishlistEntity toEntity(UserEntity user, ProductEntity product);

    @Mapping(target = "pid", source = "product.pid")
    @Mapping(target = "slug", source = "product.slug")
    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "imageUrl", source = "product.imageUrl")
    @Mapping(target = "price", source = "product.price")
    @Mapping(target = "totalStock", expression = "java(entity.getProduct().getTotalStock())")
    WishlistResponseData toResponseData(WishlistEntity entity);
}

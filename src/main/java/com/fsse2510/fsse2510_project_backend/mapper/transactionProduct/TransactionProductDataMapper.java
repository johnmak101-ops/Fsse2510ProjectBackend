package com.fsse2510.fsse2510_project_backend.mapper.transactionProduct;

import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.response.CartItemResponseData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductResponseData;
import com.fsse2510.fsse2510_project_backend.data.transactionProduct.domainObject.response.TransactionProductResponseData;
import com.fsse2510.fsse2510_project_backend.data.transactionProduct.entity.TransactionProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionProductDataMapper {
    TransactionProductResponseData toData(TransactionProductEntity entity);

    @Mapping(target = "tpid", ignore = true)
    @Mapping(target = "pid", source = "cartItem.pid")
    @Mapping(target = "sku", source = "cartItem.sku")
    @Mapping(target = "size", source = "cartItem.selectedSize")
    @Mapping(target = "color", source = "cartItem.selectedColor")
    @Mapping(target = "name", source = "cartItem.name")
    @Mapping(target = "description", source = "productData.description")
    @Mapping(target = "imageUrl", source = "cartItem.imageUrl")
    @Mapping(target = "price", source = "cartItem.price")
    @Mapping(target = "originalPrice", source = "cartItem.originalPrice")
    @Mapping(target = "discountAmount", source = "cartItem.discountAmount")
    @Mapping(target = "discountPercentage", source = "cartItem.discountPercentage")
    @Mapping(target = "quantity", source = "cartItem.cartQuantity")
    @Mapping(target = "subtotal", expression = "java(cartItem.getPrice().multiply(java.math.BigDecimal.valueOf(cartItem.getCartQuantity())))")
    TransactionProductResponseData toData(CartItemResponseData cartItem, ProductResponseData productData);
}
package com.fsse2510.fsse2510_project_backend.mapper.cartItem;

import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.response.CartItemResponseData;
import com.fsse2510.fsse2510_project_backend.data.cartitem.dto.response.CartItemResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartItemDtoMapper {
    CartItemResponseDto toResponseDto(CartItemResponseData data);
}
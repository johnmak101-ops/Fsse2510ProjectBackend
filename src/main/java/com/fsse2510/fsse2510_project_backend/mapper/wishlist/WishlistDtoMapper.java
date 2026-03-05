package com.fsse2510.fsse2510_project_backend.mapper.wishlist;

import com.fsse2510.fsse2510_project_backend.data.wishlist.domainObject.response.WishlistResponseData;
import com.fsse2510.fsse2510_project_backend.data.wishlist.dto.response.WishlistResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WishlistDtoMapper {
    @Mapping(target = "stockStatus", expression = "java(data.getTotalStock() > 0 ? \"In Stock\" : \"Out of Stock\")")
    WishlistResponseDto toResponseDto(WishlistResponseData data);
}

package com.fsse2510.fsse2510_project_backend.mapper.product;

import com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.ProductImageRequestData;
import com.fsse2510.fsse2510_project_backend.data.product.dto.request.ProductImageRequestDto;
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {

    ProductImageRequestData toRequestData(ProductImageRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "displayOrder", ignore = true)
    ProductImageEntity toEntity(ProductImageRequestData data);
}

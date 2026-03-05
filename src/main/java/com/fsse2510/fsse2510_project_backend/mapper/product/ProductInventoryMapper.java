package com.fsse2510.fsse2510_project_backend.mapper.product;

import com.fsse2510.fsse2510_project_backend.data.product.dto.response.ProductInventoryResponseDto;
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductInventoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductInventoryMapper {
    ProductInventoryResponseDto toDto(ProductInventoryEntity entity);

    com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductInventoryResponseData toResponseData(
            ProductInventoryEntity entity);

    @Mapping(target = "product", ignore = true)
    ProductInventoryEntity toEntity(
            com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.ProductInventoryRequestData data);
}

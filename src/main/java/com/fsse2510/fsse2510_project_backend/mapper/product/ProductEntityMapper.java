package com.fsse2510.fsse2510_project_backend.mapper.product;

import com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.CreateProductRequestData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.UpdateProductRequestData;
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = { ProductImageMapper.class, ProductInventoryMapper.class })
public interface ProductEntityMapper {

    @Mapping(target = "details.story", source = "story")
    @Mapping(target = "details.productIntro", source = "productIntro")
    @Mapping(target = "details.fabricInfo", source = "fabricInfo")
    @Mapping(target = "details.designStyling", source = "designStyling")
    @Mapping(target = "details.colorDisclaimer", source = "colorDisclaimer")
    @Mapping(target = "details.vendor", source = "vendor")
    @Mapping(target = "productType", source = "productType") // Map to new column
    @Mapping(target = "images", source = "images")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "collection", ignore = true)
    @Mapping(target = "pid", ignore = true)
    @Mapping(target = "promotionBadgeText", ignore = true)
    @Mapping(target = "isFeatured", ignore = true)
    @Mapping(target = "featuredPriority", ignore = true)
    @Mapping(target = "promotion", ignore = true)
    ProductEntity toEntity(CreateProductRequestData data);

    // Update Data -> Existing Entity
    @Mapping(target = "pid", ignore = true)
    @Mapping(target = "details.story", source = "details.story")
    @Mapping(target = "details.productIntro", source = "details.productIntro")
    @Mapping(target = "details.fabricInfo", source = "details.fabricInfo")
    @Mapping(target = "details.designStyling", source = "details.designStyling")
    @Mapping(target = "details.colorDisclaimer", source = "details.colorDisclaimer")
    @Mapping(target = "details.vendor", source = "details.vendor")

    @Mapping(target = "inventories", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "productType", source = "productType") // Map to new column
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "collection", ignore = true)
    @Mapping(target = "promotion", ignore = true)
    @Mapping(target = "promotionBadgeText", ignore = true)
    @Mapping(target = "isFeatured", ignore = true)
    @Mapping(target = "featuredPriority", ignore = true)
    void updateEntity(UpdateProductRequestData data, @MappingTarget ProductEntity entity);

    @AfterMapping
    default void linkCollections(@MappingTarget ProductEntity product) {
        if (product.getInventories() != null) {
            product.getInventories().forEach(inventory -> inventory.setProduct(product));
        }
        if (product.getImages() != null) {
            product.getImages().forEach(image -> image.setProduct(product));
        }
    }
}

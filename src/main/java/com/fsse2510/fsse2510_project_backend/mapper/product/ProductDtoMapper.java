package com.fsse2510.fsse2510_project_backend.mapper.product;

import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductAttributesData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductImageResponseData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductInventoryResponseData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductResponseData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductSummaryData;
import com.fsse2510.fsse2510_project_backend.data.product.dto.response.ProductAttributesResponseDto;
import com.fsse2510.fsse2510_project_backend.data.product.dto.response.ProductDetailResponseDto;
import com.fsse2510.fsse2510_project_backend.data.product.dto.response.ProductImageResponseDto;
import com.fsse2510.fsse2510_project_backend.data.product.dto.response.ProductInventoryResponseDto;
import com.fsse2510.fsse2510_project_backend.data.product.dto.response.ProductResponseDto;
import com.fsse2510.fsse2510_project_backend.data.product.dto.response.ProductSummaryResponseDto;
import com.fsse2510.fsse2510_project_backend.data.showcase.domainObject.response.ShowcaseCollectionData;
import com.fsse2510.fsse2510_project_backend.data.showcase.dto.response.ShowcaseCollectionResponseDto;
import com.fsse2510.fsse2510_project_backend.mapper.promotion.PromotionDtoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { PromotionDtoMapper.class })
public interface ProductDtoMapper {

    // List View: hasStock logic
    @Mapping(target = "hasStock", expression = "java(data.getStock() != null && data.getStock() > 0)")
    @Mapping(target = "newlyAdded", source = "isNew")
    @Mapping(target = "onSale", source = "isSale")
    @Mapping(target = "featured", source = "isFeatured")

    @Mapping(target = "slug", source = "slug")
    @Mapping(target = "status", source = "status")
    ProductResponseDto toResponseDto(ProductResponseData data);

    @Mapping(target = "newlyAdded", source = "isNew")
    @Mapping(target = "onSale", source = "isSale")
    @Mapping(target = "featured", source = "isFeatured")
    @Mapping(target = "hasStock", source = "hasStock")
    @Mapping(target = "stock", source = "stock")
    @Mapping(target = "productType", source = "productType")
    @Mapping(target = "appliedPromotionId", ignore = true)
    ProductSummaryResponseDto toSummaryResponseDto(ProductSummaryData data);

    @Mapping(target = "hasStock", expression = "java(data.getStock() != null && data.getStock() > 0)")
    @Mapping(target = "newlyAdded", source = "isNew")
    @Mapping(target = "onSale", source = "isSale")
    @Mapping(target = "featured", source = "isFeatured")
    @Mapping(target = "slug", source = "slug")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "shopifyId", source = "shopifyId")
    @Mapping(target = "vendor", source = "vendor")
    @Mapping(target = "productType", source = "productType")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "productIntro", source = "productIntro")
    @Mapping(target = "fabricInfo", source = "fabricInfo")
    @Mapping(target = "designStyling", source = "designStyling")
    @Mapping(target = "colorDisclaimer", source = "colorDisclaimer")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "images", source = "images")
    @Mapping(target = "details", source = "details")
    ProductDetailResponseDto toDetailResponseDto(ProductResponseData data);

    @Mapping(target = "id", ignore = true)
    ProductInventoryResponseDto toInventoryDto(ProductInventoryResponseData data);

    @Mapping(target = "id", ignore = true)
    ProductImageResponseDto toImageDto(ProductImageResponseData data);

    ProductAttributesResponseDto toAttributesResponseDto(ProductAttributesData data);

    default ShowcaseCollectionResponseDto toShowcaseDto(ShowcaseCollectionData data) {
        return ShowcaseCollectionResponseDto.builder()
                .id(data.getId())
                .title(data.getTitle())
                .imageUrl(data.getImageUrl())
                .bannerUrl(data.getBannerUrl())
                .tag(data.getTag())
                .build();
    }
}

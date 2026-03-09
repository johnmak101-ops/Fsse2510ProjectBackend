package com.fsse2510.fsse2510_project_backend.mapper.product;

import com.fsse2510.fsse2510_project_backend.data.product.entity.CategoryEntity;
import com.fsse2510.fsse2510_project_backend.data.product.entity.CollectionEntity;

import com.fsse2510.fsse2510_project_backend.data.product.domainObject.ProductDetails;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.CreateProductRequestData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.UpdateProductRequestData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductResponseData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductSummaryData;
import com.fsse2510.fsse2510_project_backend.data.product.dto.request.CreateProductRequestDto;
import com.fsse2510.fsse2510_project_backend.data.product.dto.request.UpdateProductMetadataRequestDto;
import com.fsse2510.fsse2510_project_backend.data.product.dto.request.UpdateProductRequestDto;
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductImageEntity; // added
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
import com.fsse2510.fsse2510_project_backend.mapper.promotion.PromotionDataMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { PromotionDataMapper.class, ProductImageMapper.class })
public interface ProductDataMapper {

    CreateProductRequestData toCreateRequestData(CreateProductRequestDto dto);

    ProductDetails toProductDetails(UpdateProductMetadataRequestDto dto);

    @Mapping(target = "pid", source = "pid")
    @Mapping(target = "category", source = "dto.category")
    @Mapping(target = "collection", source = "dto.collection")
    @Mapping(target = "isNew", source = "dto.isNew")
    @Mapping(target = "isSale", source = "dto.isSale")
    @Mapping(target = "inventories", source = "dto.inventories")
    @Mapping(target = "images", source = "dto.images")
    @Mapping(target = "vendor", source = "dto.vendor") // Added mapping
    // Map details fields from DTO to Details object in Data
    @Mapping(target = "details.story", source = "dto.story")
    @Mapping(target = "details.productIntro", source = "dto.productIntro")
    @Mapping(target = "details.fabricInfo", source = "dto.fabricInfo")
    @Mapping(target = "details.designStyling", source = "dto.designStyling")
    @Mapping(target = "details.colorDisclaimer", source = "dto.colorDisclaimer")
    @Mapping(target = "details.vendor", source = "dto.vendor") // Added mapping
    UpdateProductRequestData toUpdateRequestData(Integer pid, UpdateProductRequestDto dto);

    @Mapping(target = "promotion", source = "promotion")
    @Mapping(target = "stock", expression = "java(entity.getTotalStock())")
    @Mapping(target = "hasStock", expression = "java(entity.getTotalStock() > 0)")
    @Mapping(target = "slug", source = "slug")
    @Mapping(target = "status", source = "status")
    // @Mapping(target = "category", source = "category") // Overridden by
    // details.category
    @Mapping(target = "collection", source = "collection")
    @Mapping(target = "isNew", source = "isNew")
    @Mapping(target = "isSale", source = "isSale")
    @Mapping(target = "isFeatured", source = "isFeatured")
    @Mapping(target = "featuredPriority", source = "featuredPriority")

    // Map from JSON Details
    @Mapping(target = "story", source = "details.story")
    // @Mapping(target = "shopifyId", source = "details.shopifyId") // Removed
    @Mapping(target = "vendor", source = "details.vendor") // Uncommented
    @Mapping(target = "productType", source = "productType") // Map direct from Entity
    @Mapping(target = "category", source = "category") // Map direct from Entity
    @Mapping(target = "productIntro", source = "details.productIntro")
    @Mapping(target = "fabricInfo", source = "details.fabricInfo")
    @Mapping(target = "designStyling", source = "details.designStyling")
    @Mapping(target = "colorDisclaimer", source = "details.colorDisclaimer")

    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "images", source = "images")
    @Mapping(target = "inventories", source = "inventories")
    @Mapping(target = "details", source = "details")
    @Mapping(target = "originalPrice", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "discountPercentage", ignore = true)
    @Mapping(target = "shopifyId", ignore = true)
    ProductResponseData toResponseData(ProductEntity entity);

    @Mapping(target = "imageUrl", source = "imageUrl")
    @Mapping(target = "productType", source = "productType") // Map direct from Entity
    @Mapping(target = "isNew", source = "isNew")
    @Mapping(target = "isSale", source = "isSale")
    @Mapping(target = "isFeatured", source = "isFeatured") // Added
    @Mapping(target = "stock", expression = "java(entity.getTotalStock())")
    @Mapping(target = "hasStock", expression = "java(entity.getTotalStock() > 0)")
    @Mapping(target = "originalPrice", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "discountPercentage", ignore = true)
    @Mapping(target = "images", ignore = true) // No need to fetch all images for Listing Page
    ProductSummaryData toSummaryData(ProductEntity entity);

    // Helper: Build summary from full ResponseData (e.g., for caching fallback)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "originalPrice", source = "originalPrice")
    @Mapping(target = "discountAmount", source = "discountAmount")
    @Mapping(target = "discountPercentage", source = "discountPercentage")
    @Mapping(target = "promotionBadgeText", ignore = true)
    ProductSummaryData toSummaryDataFromResponseData(ProductResponseData data);

    default BigDecimal calculateDiscountPrice(ProductEntity entity) {
        if (entity.getPromotion() == null || !entity.getPromotion().isValidDate()) {
            return null;
        }
        PromotionEntity promo = entity.getPromotion();
        if (promo.getDiscountType() == DiscountType.PERCENTAGE) {
            // discountValue = 20 means 20% discount amount
            BigDecimal discountFactor = BigDecimal.valueOf(100).subtract(promo.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));
            return entity.getPrice().multiply(discountFactor);
        } else if (promo.getDiscountType() == DiscountType.FIXED) {
            return entity.getPrice().subtract(promo.getDiscountValue()).max(BigDecimal.ZERO);
        }
        return null;
    }

    default List<ProductSummaryData.ProductImageSummaryData> mapToImageSummary(
            List<ProductImageEntity> images) {
        if (images == null)
            return null;
        return images.stream()
                .map(img -> new ProductSummaryData.ProductImageSummaryData(img.getUrl(), img.getTag()))
                .collect(Collectors.toList());
    }

    default String map(CategoryEntity value) {
        return value != null ? value.getName() : null;
    }

    default String map(CollectionEntity value) {
        return value != null ? value.getName() : null;
    }
}

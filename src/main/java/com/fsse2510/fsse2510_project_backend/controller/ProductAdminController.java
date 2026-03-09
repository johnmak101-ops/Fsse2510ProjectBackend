package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.product.dto.request.CreateProductRequestDto;
import com.fsse2510.fsse2510_project_backend.data.product.dto.request.UpdateProductMetadataRequestDto;
import com.fsse2510.fsse2510_project_backend.data.product.dto.request.UpdateProductRequestDto;
import com.fsse2510.fsse2510_project_backend.data.product.dto.response.ProductDetailResponseDto;
import com.fsse2510.fsse2510_project_backend.mapper.product.ProductDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.product.ProductDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.ProductAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductAdminController {
        private final ProductAdminService productAdminService;
        private final ProductDataMapper productDataMapper;
        private final ProductDtoMapper productDtoMapper;

        @PostMapping
        public ProductDetailResponseDto createProduct(@RequestBody @Valid CreateProductRequestDto createDto) {
                return productDtoMapper.toDetailResponseDto(
                                productAdminService.createProduct(
                                                productDataMapper.toCreateRequestData(createDto)));
        }

        @PutMapping("/{id}")
        public ProductDetailResponseDto updateProduct(@PathVariable Integer id,
                        @RequestBody @Valid UpdateProductRequestDto updateDto) {
                return productDtoMapper.toDetailResponseDto(
                                productAdminService.updateProduct(
                                                productDataMapper.toUpdateRequestData(id, updateDto)));
        }

        @PatchMapping("/{id}/metadata")
        public ProductDetailResponseDto updateProductMetadata(@PathVariable Integer id,
                        @RequestBody UpdateProductMetadataRequestDto requestDto) {
                return productDtoMapper.toDetailResponseDto(
                                productAdminService.updateProductMetadata(id,
                                                productDataMapper.toProductDetails(requestDto)));
        }

        @DeleteMapping("/{id}")
        public ProductDetailResponseDto deleteProduct(@PathVariable Integer id) {
                return productDtoMapper.toDetailResponseDto(
                                productAdminService.deleteProduct(id));
        }
}

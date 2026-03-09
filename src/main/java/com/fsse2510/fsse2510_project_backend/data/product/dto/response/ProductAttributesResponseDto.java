package com.fsse2510.fsse2510_project_backend.data.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributesResponseDto {
    private List<String> categories;
    private List<String> sizes;
    private List<String> colors;
    private List<String> productTypes;
    private List<String> collections;
    private List<String> featuredCollections;
}

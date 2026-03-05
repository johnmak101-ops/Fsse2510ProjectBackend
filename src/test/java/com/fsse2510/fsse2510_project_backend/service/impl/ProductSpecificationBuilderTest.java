package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.ProductSearchCriteria;
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProductSpecificationBuilderTest {

    private final ProductSpecificationBuilder builder = new ProductSpecificationBuilder();

    @Test
    void fromCriteria_withCategories_shouldReturnSpecification() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .categories(List.of("men"))
                .build();

        Specification<ProductEntity> spec = builder.fromCriteria(criteria);
        assertNotNull(spec);
    }
}

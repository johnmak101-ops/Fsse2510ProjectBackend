package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.product.domainObject.request.ProductSearchCriteria;
import com.fsse2510.fsse2510_project_backend.data.product.entity.CategoryEntity;
import com.fsse2510.fsse2510_project_backend.data.product.entity.CollectionEntity;
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/*
 * Builds JPA Specifications for product search queries.
 * Extracted from ProductServiceImpl for single-responsibility.
 */
@Component
public class ProductSpecificationBuilder {

    public Specification<ProductEntity> fromCriteria(ProductSearchCriteria criteria) {
        return (root, query, cb) -> {
            if (criteria == null) {
                return cb.conjunction();
            }
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(criteria.getSearchText())) {
                String pattern = "%" + criteria.getSearchText().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("slug")), pattern)));
            }

            if (hasText(criteria.getCollection())) {
                Join<ProductEntity, CollectionEntity> join = root.join("collection");
                predicates.add(cb.or(
                        cb.equal(join.get("name"), criteria.getCollection()),
                        cb.equal(join.get("slug"), criteria.getCollection().toLowerCase())));
            }

            // Use a single join for category to be reused by both filters
            Join<ProductEntity, CategoryEntity> categoryJoin = null;

            // 1. Handle Multiple Categories (from the filter dropdown)
            if (criteria.getCategories() != null && !criteria.getCategories().isEmpty()) {
                categoryJoin = root.join("category");
                // Match by slug (lowercase) for robustness
                List<String> slugs = criteria.getCategories().stream()
                        .map(String::toLowerCase)
                        .toList();
                predicates.add(categoryJoin.get("slug").in(slugs));
            }

            // 2. Handle Single Category (from URL slug or specific query)
            if (hasText(criteria.getCategory()) && !criteria.getCategory().equalsIgnoreCase("all")) {
                if (categoryJoin == null)
                    categoryJoin = root.join("category");
                predicates.add(cb.or(
                        cb.equal(cb.lower(categoryJoin.get("slug")), criteria.getCategory().toLowerCase()),
                        cb.equal(cb.lower(categoryJoin.get("name")), criteria.getCategory().toLowerCase())));
            }

            addPriceRange(predicates, cb, root, criteria.getMinPrice(), criteria.getMaxPrice());

            if (hasText(criteria.getProductType())) {
                predicates.add(cb.equal(root.get("productType"), criteria.getProductType()));
            }

            if (criteria.getIsNew() != null) {
                predicates.add(cb.equal(root.get("isNew"), criteria.getIsNew()));
            }

            if (criteria.getLastPid() != null
                    && (criteria.getSortBy() == null || criteria.getSortBy().equalsIgnoreCase("newest"))) {
                predicates.add(cb.lessThan(root.get("pid"), criteria.getLastPid()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addPriceRange(List<Predicate> predicates, CriteriaBuilder cb,
            Root<ProductEntity> root, BigDecimal min, BigDecimal max) {
        if (min != null)
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), min));
        if (max != null)
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), max));
    }

    private boolean hasText(String str) {
        return str != null && !str.isBlank();
    }
}

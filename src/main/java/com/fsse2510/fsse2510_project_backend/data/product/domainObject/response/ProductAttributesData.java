package com.fsse2510.fsse2510_project_backend.data.product.domainObject.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.List;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class ProductAttributesData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<String> categories;
    private List<String> sizes;
    private List<String> colors;
    private List<String> productTypes;
    private List<String> collections;
    private List<String> featuredCollections;
}

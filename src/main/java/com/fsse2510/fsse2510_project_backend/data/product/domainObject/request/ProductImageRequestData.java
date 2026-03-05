package com.fsse2510.fsse2510_project_backend.data.product.domainObject.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageRequestData {
    private Integer id;
    private String url;
    private String tag;
}

package com.fsse2510.fsse2510_project_backend.data.product.domainObject.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponseData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String url;
    private String tag;
}

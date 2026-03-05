package com.fsse2510.fsse2510_project_backend.data.wishlist.domainObject.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponseData {
    private Integer pid;
    private String slug;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private Integer totalStock;
}

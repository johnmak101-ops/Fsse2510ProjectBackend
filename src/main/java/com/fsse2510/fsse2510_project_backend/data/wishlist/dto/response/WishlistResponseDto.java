package com.fsse2510.fsse2510_project_backend.data.wishlist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponseDto {
    private Integer pid;
    private String slug;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private String stockStatus; // e.g., "In Stock", "Out of Stock"
}

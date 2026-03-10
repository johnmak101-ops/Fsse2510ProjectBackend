package com.fsse2510.fsse2510_project_backend.data.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageRequestDto {
    private Integer id;

    @NotBlank(message = "Image URL is required")
    private String url;

    private String tag;
}

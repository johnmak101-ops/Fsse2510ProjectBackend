package com.fsse2510.fsse2510_project_backend.data.showcase.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShowcaseCollectionResponseDto {
    private Integer id;
    private String title;
    private String imageUrl; // Renamed from image to match frontend types
    private String bannerUrl;
    private String tag;
}

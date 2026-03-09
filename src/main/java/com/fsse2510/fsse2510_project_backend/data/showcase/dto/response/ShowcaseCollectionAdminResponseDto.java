package com.fsse2510.fsse2510_project_backend.data.showcase.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShowcaseCollectionAdminResponseDto {
    private Integer id;
    private String title;
    private String imageUrl;
    private String bannerUrl;
    private String tag;
    private Integer orderIndex;
    private boolean active;
}

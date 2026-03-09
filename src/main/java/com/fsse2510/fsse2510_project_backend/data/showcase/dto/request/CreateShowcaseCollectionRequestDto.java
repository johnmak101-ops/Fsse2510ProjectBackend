package com.fsse2510.fsse2510_project_backend.data.showcase.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateShowcaseCollectionRequestDto {
    @NotBlank(message = "Title cannot be empty")
    private String title;
    private String imageUrl;
    private String bannerUrl;
    @NotBlank(message = "Tag cannot be empty")
    private String tag;
    private Integer orderIndex;
    private Boolean active;
}

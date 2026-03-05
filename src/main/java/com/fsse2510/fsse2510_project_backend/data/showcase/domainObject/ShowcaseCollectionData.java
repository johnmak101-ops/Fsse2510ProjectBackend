package com.fsse2510.fsse2510_project_backend.data.showcase.domainObject;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowcaseCollectionData {
    private Integer id;
    private String title;
    private String imageUrl;
    private String bannerUrl;
    private String tag;
}

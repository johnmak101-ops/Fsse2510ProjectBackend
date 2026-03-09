package com.fsse2510.fsse2510_project_backend.data.showcase.domainObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowcaseCollectionAdminData {
    private Integer id;
    private String title;
    private String imageUrl;
    private String bannerUrl;
    private String tag;
    private Integer orderIndex;
    private boolean active;
}

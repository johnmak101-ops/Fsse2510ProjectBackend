package com.fsse2510.fsse2510_project_backend.data.showcase.domainObject;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowcaseCollectionData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String title;
    private String imageUrl;
    private String bannerUrl;
    private String tag;
}

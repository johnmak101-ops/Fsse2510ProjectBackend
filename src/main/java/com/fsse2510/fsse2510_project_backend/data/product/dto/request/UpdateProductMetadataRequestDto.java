package com.fsse2510.fsse2510_project_backend.data.product.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateProductMetadataRequestDto {
    private String story;
    private String productIntro;
    private String fabricInfo;
    private String designStyling;
    private String colorDisclaimer;
    private String vendor;
}

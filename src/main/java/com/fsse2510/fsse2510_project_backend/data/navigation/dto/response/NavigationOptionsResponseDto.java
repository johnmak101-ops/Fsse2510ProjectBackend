package com.fsse2510.fsse2510_project_backend.data.navigation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationOptionsResponseDto {
    @JsonProperty("collections")
    private List<String> collections;

    @JsonProperty("categories")
    private List<String> categories;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("productTypes")
    private List<String> productTypes;
}

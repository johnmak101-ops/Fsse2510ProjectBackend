package com.fsse2510.fsse2510_project_backend.data.navigation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNavigationItemRequestDto {
    @JsonProperty("label")
    private String label;

    @JsonProperty("type")
    private String type;

    @JsonProperty("action_type")
    private String actionType;

    @JsonProperty("action_value")
    private String actionValue;

    @JsonProperty("parent_id")
    private Integer parentId;

    @JsonProperty("sort_order")
    private Integer sortOrder;

    @JsonProperty("is_new")
    private Boolean isNew;

    @JsonProperty("is_active")
    private Boolean isActive;
}

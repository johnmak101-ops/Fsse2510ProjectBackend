package com.fsse2510.fsse2510_project_backend.data.navigation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNavigationItemRequestDto {
    @JsonProperty("id")
    private Integer id;

    @NotBlank(message = "Label is required")
    @JsonProperty("label")
    private String label;

    @NotBlank(message = "Type is required")
    @JsonProperty("type")
    private String type;

    @NotBlank(message = "Action type is required")
    @JsonProperty("action_type")
    private String actionType;

    @NotBlank(message = "Action value is required")
    @JsonProperty("action_value")
    private String actionValue;

    @JsonProperty("parent_id")
    private Integer parentId;

    @NotNull(message = "Sort order is required")
    @JsonProperty("sort_order")
    private Integer sortOrder;

    @JsonProperty("is_new")
    private Boolean isNew;

    @JsonProperty("is_active")
    private Boolean isActive;
}

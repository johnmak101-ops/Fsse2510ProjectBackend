package com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNavigationItemRequestData {
    private Integer id;
    private String label;
    private String type;
    private String actionType;
    private String actionValue;
    private Integer parentId;
    private Integer sortOrder;
    private Boolean isNew;
    private Boolean isActive;
}

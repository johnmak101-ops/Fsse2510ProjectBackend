package com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationItemData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String label;
    private String type;
    private String actionType;
    private String actionValue;
    private Integer parentId;
    private List<NavigationItemData> children;
    private Integer sortOrder;
    private Boolean isNew;
    private Boolean isActive;
}

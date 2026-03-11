package com.fsse2510.fsse2510_project_backend.mapper.navigation;

import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.request.CreateNavigationItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.navigation.entity.NavigationItemEntity;
import org.springframework.stereotype.Component;

@Component
public class NavigationItemEntityMapper {

    public NavigationItemEntity toEntity(CreateNavigationItemRequestData data) {
        if (data == null) {
            return null;
        }
        return NavigationItemEntity.builder()
                .label(data.getLabel())
                .type(data.getType())
                .actionType(data.getActionType())
                .actionValue(data.getActionValue())
                .sortOrder(data.getSortOrder())
                .isNew(data.getIsNew())
                .isActive(data.getIsActive())
                // Parent must be set by Service
                .build();
    }
}

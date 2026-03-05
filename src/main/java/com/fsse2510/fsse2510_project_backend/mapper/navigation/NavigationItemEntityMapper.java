package com.fsse2510.fsse2510_project_backend.mapper.navigation;

import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.request.CreateNavigationItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.response.NavigationItemData;
import com.fsse2510.fsse2510_project_backend.data.navigation.entity.NavigationItemEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class NavigationItemEntityMapper {

    public NavigationItemData toData(NavigationItemEntity entity) {
        if (entity == null) {
            return null;
        }
        return NavigationItemData.builder()
                .id(entity.getId())
                .label(entity.getLabel())
                .type(entity.getType())
                .actionType(entity.getActionType())
                .actionValue(entity.getActionValue())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .sortOrder(entity.getSortOrder())
                .isNew(entity.getIsNew())
                .isActive(entity.getIsActive())
                .children(entity.getChildren() != null
                        ? entity.getChildren().stream().map(this::toData).collect(Collectors.toList())
                        : Collections.emptyList())
                .build();
    }

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

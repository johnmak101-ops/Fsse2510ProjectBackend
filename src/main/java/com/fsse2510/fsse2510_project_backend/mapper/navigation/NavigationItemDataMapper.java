package com.fsse2510.fsse2510_project_backend.mapper.navigation;

import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.request.CreateNavigationItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.request.UpdateNavigationItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.response.NavigationItemData;
import com.fsse2510.fsse2510_project_backend.data.navigation.dto.request.CreateNavigationItemRequestDto;
import com.fsse2510.fsse2510_project_backend.data.navigation.dto.request.UpdateNavigationItemRequestDto;
import com.fsse2510.fsse2510_project_backend.data.navigation.entity.NavigationItemEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class NavigationItemDataMapper {

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
                        : new ArrayList<>())
                .build();
    }

    public CreateNavigationItemRequestData toCreateRequestData(CreateNavigationItemRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return CreateNavigationItemRequestData.builder()
                .label(dto.getLabel())
                .type(dto.getType())
                .actionType(dto.getActionType())
                .actionValue(dto.getActionValue())
                .parentId(dto.getParentId())
                .sortOrder(dto.getSortOrder())
                .isNew(dto.getIsNew())
                .isActive(dto.getIsActive())
                .build();
    }

    public UpdateNavigationItemRequestData toUpdateRequestData(Integer id, UpdateNavigationItemRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return UpdateNavigationItemRequestData.builder()
                .id(id)
                .label(dto.getLabel())
                .type(dto.getType())
                .actionType(dto.getActionType())
                .actionValue(dto.getActionValue())
                .parentId(dto.getParentId())
                .sortOrder(dto.getSortOrder())
                .isNew(dto.getIsNew())
                .isActive(dto.getIsActive())
                .build();
    }
}

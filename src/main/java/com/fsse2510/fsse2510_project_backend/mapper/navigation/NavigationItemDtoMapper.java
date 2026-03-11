package com.fsse2510.fsse2510_project_backend.mapper.navigation;

import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.response.NavigationItemData;
import com.fsse2510.fsse2510_project_backend.data.navigation.dto.response.NavigationItemResponseDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

import com.fsse2510.fsse2510_project_backend.data.navigation.dto.response.NavigationOptionsResponseDto;

@Component
public class NavigationItemDtoMapper {

    public NavigationItemResponseDto toResponseDto(NavigationItemData data) {
        if (data == null) {
            return null;
        }
        return NavigationItemResponseDto.builder()
                .id(data.getId())
                .label(data.getLabel())
                .type(data.getType())
                .actionType(data.getActionType())
                .actionValue(data.getActionValue())
                .parentId(data.getParentId())
                .sortOrder(data.getSortOrder())
                .isNew(data.getIsNew())
                .isActive(data.getIsActive())
                .children(data.getChildren() != null
                        ? data.getChildren().stream().map(this::toResponseDto).collect(Collectors.toList())
                        : Collections.emptyList())
                .build();
    }

    public NavigationOptionsResponseDto toOptionsResponseDto(
            com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.response.NavigationOptionsData data) {
        if (data == null) {
            return null;
        }
        return NavigationOptionsResponseDto.builder()
                .collections(data.getCollections())
                .categories(data.getCategories())
                .tags(data.getTags())
                .productTypes(data.getProductTypes())
                .build();
    }
}

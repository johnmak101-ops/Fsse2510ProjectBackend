package com.fsse2510.fsse2510_project_backend.mapper.navigation;

import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.request.CreateNavigationItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.request.UpdateNavigationItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.response.NavigationItemData;
import com.fsse2510.fsse2510_project_backend.data.navigation.dto.request.CreateNavigationItemRequestDto;
import com.fsse2510.fsse2510_project_backend.data.navigation.dto.request.UpdateNavigationItemRequestDto;
import com.fsse2510.fsse2510_project_backend.data.navigation.dto.response.NavigationItemResponseDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

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

    public com.fsse2510.fsse2510_project_backend.data.navigation.dto.response.NavigationOptionsResponseDto toOptionsResponseDto(
            com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.response.NavigationOptionsData data) {
        if (data == null) {
            return null;
        }
        return com.fsse2510.fsse2510_project_backend.data.navigation.dto.response.NavigationOptionsResponseDto.builder()
                .collections(data.getCollections())
                .categories(data.getCategories())
                .tags(data.getTags())
                .productTypes(data.getProductTypes())
                .build();
    }
}

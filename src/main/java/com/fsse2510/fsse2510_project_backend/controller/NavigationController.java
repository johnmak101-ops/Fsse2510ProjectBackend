package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.request.CreateNavigationItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.request.UpdateNavigationItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.response.NavigationItemData;
import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.response.NavigationOptionsData;
import com.fsse2510.fsse2510_project_backend.data.navigation.dto.request.CreateNavigationItemRequestDto;
import com.fsse2510.fsse2510_project_backend.data.navigation.dto.request.UpdateNavigationItemRequestDto;
import com.fsse2510.fsse2510_project_backend.data.navigation.dto.response.NavigationItemResponseDto;
import com.fsse2510.fsse2510_project_backend.data.navigation.dto.response.NavigationOptionsResponseDto;
import com.fsse2510.fsse2510_project_backend.mapper.navigation.NavigationItemDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.NavigationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class NavigationController {

    private final NavigationService navigationService;
    private final NavigationItemDtoMapper navigationItemDtoMapper;

    /**
     * Retrieves the public navigation menu for the shop frontend.
     * <p>
     * Endpoint: GET /public/navigation
     * Access: Public
     * </p>
     *
     * @return A list of NavigationItemResponseDto representing the navbar
     *         structure.
     */
    @GetMapping("/public/navigation")
    public List<NavigationItemResponseDto> getPublicNavigation() {
        List<NavigationItemData> dataList = navigationService.getPublicNavigation();
        return dataList.stream()
                .map(navigationItemDtoMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves navigation options (categories/brands) for admin dropdowns.
     * <p>
     * Endpoint: GET /admin/navigation/options
     * Access: ADMIN only
     * </p>
     *
     * @return NavigationOptionsResponseDto containing available options for
     *         creating nav items.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/navigation/options")
    public NavigationOptionsResponseDto getNavigationOptions() {
        NavigationOptionsData data = navigationService.getNavigationOptions();
        return navigationItemDtoMapper.toOptionsResponseDto(data);
    }

    /**
     * Creates a new navigation item.
     * <p>
     * Endpoint: POST /admin/navigation
     * Access: ADMIN only
     * </p>
     *
     * @param createDto DTO containing details for the new navigation item.
     * @return The created NavigationItemResponseDto.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/navigation")
    public NavigationItemResponseDto createItem(@RequestBody CreateNavigationItemRequestDto createDto) {
        CreateNavigationItemRequestData createData = navigationItemDtoMapper.toCreateRequestData(createDto);
        NavigationItemData result = navigationService.createItem(createData);
        return navigationItemDtoMapper.toResponseDto(result);
    }

    /**
     * Updates an existing navigation item.
     * <p>
     * Endpoint: PUT /admin/navigation/{id}
     * Access: ADMIN only
     * </p>
     *
     * @param id        The ID of the navigation item to update.
     * @param updateDto DTO containing updated details.
     * @return The updated NavigationItemResponseDto.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/navigation/{id}")
    public NavigationItemResponseDto updateItem(@PathVariable Integer id,
            @RequestBody UpdateNavigationItemRequestDto updateDto) {
        UpdateNavigationItemRequestData updateData = navigationItemDtoMapper.toUpdateRequestData(id, updateDto);
        NavigationItemData result = navigationService.updateItem(updateData);
        return navigationItemDtoMapper.toResponseDto(result);
    }

    /**
     * Deletes a navigation item.
     * <p>
     * Endpoint: DELETE /admin/navigation/{id}
     * Access: ADMIN only
     * </p>
     *
     * @param id The ID of the navigation item to delete.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/navigation/{id}")
    public void deleteItem(@PathVariable Integer id) {
        navigationService.deleteItem(id);
    }

    /**
     * Manually initializes or migrates initial navigation data.
     * <p>
     * Endpoint: POST /admin/navigation/init
     * Access: ADMIN only
     * </p>
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/navigation/init")
    public void initData() {
        navigationService.migrateInitialData();
    }
}

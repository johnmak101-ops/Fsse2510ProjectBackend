package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.request.CreateNavigationItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.request.UpdateNavigationItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.response.NavigationItemData;
import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.response.NavigationOptionsData;

import java.util.List;

public interface NavigationService {
    List<NavigationItemData> getPublicNavigation();

    NavigationItemData createItem(CreateNavigationItemRequestData createData);

    NavigationItemData updateItem(UpdateNavigationItemRequestData updateData);

    void deleteItem(Integer id);

    NavigationOptionsData getNavigationOptions();

    void migrateInitialData();
}

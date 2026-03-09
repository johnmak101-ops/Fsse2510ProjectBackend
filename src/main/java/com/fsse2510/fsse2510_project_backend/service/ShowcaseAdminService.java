package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.showcase.domainObject.ShowcaseCollectionAdminData;

import java.util.List;

public interface ShowcaseAdminService {

    List<ShowcaseCollectionAdminData> getAll();

    ShowcaseCollectionAdminData create(ShowcaseCollectionAdminData data);

    ShowcaseCollectionAdminData update(Integer id, ShowcaseCollectionAdminData data);

    void delete(Integer id);
}

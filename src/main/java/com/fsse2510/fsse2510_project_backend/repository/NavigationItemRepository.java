package com.fsse2510.fsse2510_project_backend.repository;

import com.fsse2510.fsse2510_project_backend.data.navigation.entity.NavigationItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface NavigationItemRepository extends JpaRepository<NavigationItemEntity, Integer> {

    // Fetch roots with children eagerly to prevent N+1
    // Note: 'children' is a OneToMany, so we use LEFT JOIN FETCH.
    // We only want roots (parent IS NULL)
    @Query("SELECT DISTINCT n FROM NavigationItemEntity n LEFT JOIN FETCH n.children WHERE n.parent IS NULL AND n.isActive = true ORDER BY n.sortOrder ASC")
    List<NavigationItemEntity> findPublicNavigationRoots();

    // Standard find for Admin (Fetch all flat or specific queries, usually we want
    // all to show tree)
    List<NavigationItemEntity> findAllByOrderBySortOrderAsc();

    // For migration check
    long count();
}

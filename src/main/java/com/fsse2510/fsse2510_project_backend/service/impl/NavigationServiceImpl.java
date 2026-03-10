package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.request.CreateNavigationItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.request.UpdateNavigationItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.response.NavigationItemData;
import com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.response.NavigationOptionsData;
import com.fsse2510.fsse2510_project_backend.data.navigation.entity.NavigationItemEntity;
import com.fsse2510.fsse2510_project_backend.data.config.entity.SystemConfigEntity;
import com.fsse2510.fsse2510_project_backend.mapper.navigation.NavigationItemEntityMapper;
import com.fsse2510.fsse2510_project_backend.repository.CategoryRepository;
import com.fsse2510.fsse2510_project_backend.repository.CollectionRepository;
import com.fsse2510.fsse2510_project_backend.repository.NavigationItemRepository;
import com.fsse2510.fsse2510_project_backend.repository.ProductRepository;
import com.fsse2510.fsse2510_project_backend.repository.SystemConfigRepository;
import com.fsse2510.fsse2510_project_backend.service.NavigationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import com.fsse2510.fsse2510_project_backend.data.product.entity.CategoryEntity;
import com.fsse2510.fsse2510_project_backend.data.product.entity.CollectionEntity;

@Service
@RequiredArgsConstructor
public class NavigationServiceImpl implements NavigationService {

    // This class is for Navbar CMS
    private final NavigationItemRepository navigationItemRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CollectionRepository collectionRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final NavigationItemEntityMapper navigationItemEntityMapper;

    private static final String CACHE_NAVIGATION = "navigation_v3";

    @Override
    @Transactional
    @Cacheable(value = CACHE_NAVIGATION, key = "'public'", sync = true)
    public List<NavigationItemData> getPublicNavigation() {
        List<NavigationItemEntity> roots = navigationItemRepository.findPublicNavigationRoots();
        return roots.stream()
                .map(navigationItemEntityMapper::toData)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAVIGATION, allEntries = true)
    public NavigationItemData createItem(CreateNavigationItemRequestData createData) {
        NavigationItemEntity entity = navigationItemEntityMapper.toEntity(createData);

        if (createData.getParentId() != null) {
            NavigationItemEntity parent = navigationItemRepository.findById(createData.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Parent Navigation Item not found: " + createData.getParentId()));
            entity.setParent(parent);
            parent.getChildren().add(entity);
        }

        NavigationItemEntity saved = navigationItemRepository.save(entity);
        return navigationItemEntityMapper.toData(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAVIGATION, allEntries = true)
    public NavigationItemData updateItem(UpdateNavigationItemRequestData updateData) {
        NavigationItemEntity entity = navigationItemRepository.findById(updateData.getId())
                .orElseThrow(() -> new EntityNotFoundException("Navigation Item not found: " + updateData.getId()));

        if (updateData.getLabel() != null)
            entity.setLabel(updateData.getLabel());
        if (updateData.getType() != null)
            entity.setType(updateData.getType());
        if (updateData.getActionType() != null)
            entity.setActionType(updateData.getActionType());
        if (updateData.getActionValue() != null)
            entity.setActionValue(updateData.getActionValue());
        if (updateData.getSortOrder() != null)
            entity.setSortOrder(updateData.getSortOrder());
        if (updateData.getIsNew() != null)
            entity.setIsNew(updateData.getIsNew());
        if (updateData.getIsActive() != null)
            entity.setIsActive(updateData.getIsActive());

        if (updateData.getParentId() != null) {
            if (entity.getParent() == null || !entity.getParent().getId().equals(updateData.getParentId())) {
                NavigationItemEntity newParent = navigationItemRepository.findById(updateData.getParentId())
                        .orElseThrow(
                                () -> new EntityNotFoundException("New Parent not found: " + updateData.getParentId()));
                entity.setParent(newParent);
            }
        }

        NavigationItemEntity saved = navigationItemRepository.save(entity);
        return navigationItemEntityMapper.toData(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAVIGATION, allEntries = true)
    public void deleteItem(Integer id) {
        if (navigationItemRepository.existsById(id)) {
            navigationItemRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException("Navigation Item not found: " + id);
        }
    }

    @Override
    public NavigationOptionsData getNavigationOptions() {
        return NavigationOptionsData.builder()
                .collections(collectionRepository.findAll().stream()
                        .map(CollectionEntity::getName)
                        .toList())
                .categories(categoryRepository.findAll().stream()
                        .map(CategoryEntity::getName)
                        .toList())
                .tags(productRepository.findAllDistinctTags())
                .productTypes(productRepository.findAllDistinctProductTypes())
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAVIGATION, allEntries = true)
    public void migrateInitialData() {
        if (navigationItemRepository.count() > 0) {
            return;
        }

        // for no data in db use
        createRoot("WOMEN", "/collections/women", 10);
        createRoot("MEN", "/collections/men", 20);
        createRoot("PET", "/collections/pet", 30);
        createRoot("HOME", "/collections/home", 40);

        NavigationItemData collectionsTab = createRoot("COLLECTIONS", "#", 50);

        String configVal = systemConfigRepository.findByConfigKey("navbar_featured_collections")
                .map(SystemConfigEntity::getConfigValue)
                .orElse("PEANUTS,Classic,GELATO");

        String[] featured = configVal.split(",");
        int order = 10;
        for (String col : featured) {
            String colName = col.trim();
            if (!colName.isEmpty()) {
                createChild(collectionsTab.getId(), colName, "FILTER_COLLECTION", colName, order);
                order += 10;
            }
        }
    }

    private NavigationItemData createRoot(String label, String url, int order) {
        return createItem(CreateNavigationItemRequestData.builder()
                .label(label)
                .type("TAB")
                .actionType("URL")
                .actionValue(url)
                .sortOrder(order)
                .isNew(false)
                .isActive(true)
                .build());
    }

    private void createChild(Integer parentId, String label, String actionType, String actionValue, int order) {
        createItem(CreateNavigationItemRequestData.builder()
                .label(label)
                .type("DROPDOWN_ITEM")
                .actionType(actionType)
                .actionValue(actionValue)
                .parentId(parentId)
                .sortOrder(order)
                .isNew(false)
                .isActive(true)
                .build());
    }
}

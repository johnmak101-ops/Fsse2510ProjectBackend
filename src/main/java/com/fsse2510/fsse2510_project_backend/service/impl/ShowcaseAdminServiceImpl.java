package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.showcase.domainObject.ShowcaseCollectionAdminData;
import com.fsse2510.fsse2510_project_backend.data.showcase.entity.ShowcaseCollectionEntity;
import com.fsse2510.fsse2510_project_backend.exception.showcase.ShowcaseCollectionNotFoundException;
import com.fsse2510.fsse2510_project_backend.mapper.showcase.ShowcaseEntityMapper;
import com.fsse2510.fsse2510_project_backend.repository.ShowcaseCollectionRepository;
import com.fsse2510.fsse2510_project_backend.service.ShowcaseAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowcaseAdminServiceImpl implements ShowcaseAdminService {

    private static final String CACHE_SHOWCASE_COLLECTIONS = "showcase_collections_v3";

    private final ShowcaseCollectionRepository showcaseCollectionRepository;
    private final ShowcaseEntityMapper showcaseEntityMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ShowcaseCollectionAdminData> getAll() {
        return showcaseCollectionRepository.findAll().stream()
                .map(showcaseEntityMapper::toAdminData)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_SHOWCASE_COLLECTIONS, allEntries = true)
    public ShowcaseCollectionAdminData create(ShowcaseCollectionAdminData data) {
        ShowcaseCollectionEntity entity = ShowcaseCollectionEntity.builder()
                .title(data.getTitle())
                .imageUrl(data.getImageUrl())
                .bannerUrl(data.getBannerUrl())
                .tag(data.getTag())
                .orderIndex(data.getOrderIndex())
                .active(data.isActive())
                .build();

        ShowcaseCollectionEntity saved = showcaseCollectionRepository.save(entity);
        return showcaseEntityMapper.toAdminData(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_SHOWCASE_COLLECTIONS, allEntries = true)
    public ShowcaseCollectionAdminData update(Integer id, ShowcaseCollectionAdminData data) {
        ShowcaseCollectionEntity existing = showcaseCollectionRepository.findById(id)
                .orElseThrow(() -> new ShowcaseCollectionNotFoundException(id));

        existing.setTitle(data.getTitle());
        existing.setImageUrl(data.getImageUrl());
        existing.setBannerUrl(data.getBannerUrl());
        existing.setTag(data.getTag());
        existing.setOrderIndex(data.getOrderIndex());
        existing.setActive(data.isActive());

        ShowcaseCollectionEntity saved = showcaseCollectionRepository.save(existing);
        return showcaseEntityMapper.toAdminData(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_SHOWCASE_COLLECTIONS, allEntries = true)
    public void delete(Integer id) {
        if (!showcaseCollectionRepository.existsById(id)) {
            throw new ShowcaseCollectionNotFoundException(id);
        }
        showcaseCollectionRepository.deleteById(id);
    }
}

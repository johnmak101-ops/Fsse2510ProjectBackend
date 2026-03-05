package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.showcase.entity.ShowcaseCollectionEntity;
import com.fsse2510.fsse2510_project_backend.repository.ShowcaseCollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/showcase/collections")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ShowcaseAdminController {
    private final ShowcaseCollectionRepository repository;

    //Homepage Collection Slider API
    @GetMapping
    public List<ShowcaseCollectionEntity> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public ShowcaseCollectionEntity create(@RequestBody ShowcaseCollectionEntity entity) {
        return repository.save(entity);
    }

    @PutMapping("/{id}")
    public ShowcaseCollectionEntity update(@PathVariable Integer id, @RequestBody ShowcaseCollectionEntity entity) {
        ShowcaseCollectionEntity existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        existing.setTitle(entity.getTitle());
        existing.setImageUrl(entity.getImageUrl());
        existing.setBannerUrl(entity.getBannerUrl());
        existing.setTag(entity.getTag());
        existing.setOrderIndex(entity.getOrderIndex());
        existing.setActive(entity.isActive());

        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        repository.deleteById(id);
    }
}

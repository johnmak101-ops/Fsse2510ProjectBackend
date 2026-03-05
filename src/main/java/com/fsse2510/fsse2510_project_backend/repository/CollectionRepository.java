package com.fsse2510.fsse2510_project_backend.repository;

import com.fsse2510.fsse2510_project_backend.data.product.entity.CollectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CollectionRepository extends JpaRepository<CollectionEntity, Integer> {
    Optional<CollectionEntity> findByName(String name);

    Optional<CollectionEntity> findBySlug(String slug);
}

package com.fsse2510.fsse2510_project_backend.repository;

import com.fsse2510.fsse2510_project_backend.data.showcase.entity.ShowcaseCollectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowcaseCollectionRepository extends JpaRepository<ShowcaseCollectionEntity, Integer> {
    List<ShowcaseCollectionEntity> findAllByActiveTrueOrderByOrderIndexAsc();
}

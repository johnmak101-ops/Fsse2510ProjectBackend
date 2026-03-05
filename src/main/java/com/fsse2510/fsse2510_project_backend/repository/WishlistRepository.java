package com.fsse2510.fsse2510_project_backend.repository;

import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import com.fsse2510.fsse2510_project_backend.data.wishlist.entity.WishlistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistEntity, Integer> {
    // Optimization: Eager fetch product to avoid N+1 queries
    // Without JOIN FETCH: 1 + N queries (1 for wishlist + N for products)
    // With JOIN FETCH: 1 query only
    @Query("SELECT DISTINCT w FROM WishlistEntity w " +
            "JOIN FETCH w.product p " +
            "WHERE w.user = :user")
    List<WishlistEntity> findAllByUser(@Param("user") UserEntity user);

    Optional<WishlistEntity> findByUserAndProduct(UserEntity user, ProductEntity product);

    void deleteByUserAndProduct(UserEntity user, ProductEntity product);
}

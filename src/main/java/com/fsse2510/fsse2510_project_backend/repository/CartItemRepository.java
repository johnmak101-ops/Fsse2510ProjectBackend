package com.fsse2510.fsse2510_project_backend.repository;

import com.fsse2510.fsse2510_project_backend.data.cartitem.entity.CartItemEntity;
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductInventoryEntity;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Integer> {

        @Query("SELECT c FROM CartItemEntity c " +
                        "JOIN FETCH c.productInventory pi " +
                        "JOIN FETCH pi.product p " +
                        "LEFT JOIN FETCH p.category " +
                        "LEFT JOIN FETCH p.collection " +
                        "LEFT JOIN FETCH p.promotion " +
                        "WHERE c.user = :user")
        List<CartItemEntity> findAllByUserWithProduct(
                        @Param("user") UserEntity user);

        Optional<CartItemEntity> findByUserAndProductInventory(UserEntity user,
                        ProductInventoryEntity productInventory);

        // JPQL bulk delete - @Modifying with placeholder user and inventory
        @Modifying
        @Query("DELETE FROM CartItemEntity c WHERE c.user = :user AND c.productInventory = :inventory")
        void deleteByUserAndProductInventory(
                        @Param("user") UserEntity user,
                        @Param("inventory") ProductInventoryEntity inventory);

        // JPQL bulk delete — single DELETE SQL, avoids SELECT+N issues
        @Modifying
        @Query("DELETE FROM CartItemEntity c WHERE c.user = :user")
        void deleteAllByUser(@Param("user") UserEntity user);
}

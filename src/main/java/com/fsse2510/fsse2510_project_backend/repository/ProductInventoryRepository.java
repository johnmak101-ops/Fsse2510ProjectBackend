package com.fsse2510.fsse2510_project_backend.repository;

import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductInventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductInventoryRepository extends JpaRepository<ProductInventoryEntity, Integer> {
    Optional<ProductInventoryEntity> findBySku(String sku);

    @Query("SELECT pi FROM ProductInventoryEntity pi JOIN FETCH pi.product WHERE pi.sku IN :skus")
    java.util.List<ProductInventoryEntity> findBySkuInWithProduct(@Param("skus") java.util.List<String> skus);

    @Modifying
    @Query(value = "UPDATE product_inventory SET stock = stock - :quantity WHERE sku = :sku AND stock >= :quantity", nativeQuery = true)
    int deductStock(@Param("sku") String sku,
            @Param("quantity") Integer quantity);
}

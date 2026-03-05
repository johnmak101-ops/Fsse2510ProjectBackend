package com.fsse2510.fsse2510_project_backend.data.cartitem.entity;

import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductInventoryEntity;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_item", indexes = {
        @Index(name = "idx_cart_item_user_inventory", columnList = "uid, inventory_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cid")
    private Integer cid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private ProductInventoryEntity productInventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    private UserEntity user;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}

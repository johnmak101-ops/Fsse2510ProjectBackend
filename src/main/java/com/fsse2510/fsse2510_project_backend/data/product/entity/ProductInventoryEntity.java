package com.fsse2510.fsse2510_project_backend.data.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInventoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pid", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ProductEntity product;

    @Column(nullable = false, unique = true)
    private String sku;

    private String size;

    private String color;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "stock_reserved")
    private Integer stockReserved;

    @Column(name = "weight")
    private java.math.BigDecimal weight;
}

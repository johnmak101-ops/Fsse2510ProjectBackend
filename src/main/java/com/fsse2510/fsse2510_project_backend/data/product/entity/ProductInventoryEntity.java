package com.fsse2510.fsse2510_project_backend.data.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

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
    private BigDecimal weight;
}

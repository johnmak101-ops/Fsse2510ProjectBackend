package com.fsse2510.fsse2510_project_backend.data.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_image")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pid", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ProductEntity product;

    @Column(nullable = false)
    private String url;

    @Column(name = "tag")
    private String tag; // Represents color (e.g. "Pink") or usage (e.g. "Detail", "Lifestyle")

    @Column(name = "display_order")
    private Integer displayOrder;
}

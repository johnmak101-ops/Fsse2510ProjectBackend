package com.fsse2510.fsse2510_project_backend.data.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_collection")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "collection")
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<ProductEntity> products = new ArrayList<>();
}

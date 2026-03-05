package com.fsse2510.fsse2510_project_backend.data.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @OneToMany(mappedBy = "category")
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<ProductEntity> products = new ArrayList<>();
}

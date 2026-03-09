package com.fsse2510.fsse2510_project_backend.data.showcase.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "showcase_collection")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowcaseCollectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Column(nullable = false)
    private String tag;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(nullable = false)
    private boolean active = true;
}

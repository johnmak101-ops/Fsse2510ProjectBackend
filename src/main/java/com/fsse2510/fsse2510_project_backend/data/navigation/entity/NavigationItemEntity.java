package com.fsse2510.fsse2510_project_backend.data.navigation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "navigation_item", indexes = {
        @Index(name = "idx_nav_parent", columnList = "parent_id"),
        @Index(name = "idx_nav_sort", columnList = "sort_order")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String type; // TAB, DROPDOWN_ITEM

    @Column(name = "action_type")
    private String actionType; // FILTER_COLLECTION, FILTER_CATEGORY, FILTER_CUSTOM, URL

    @Column(name = "action_value")
    private String actionValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private NavigationItemEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<NavigationItemEntity> children = new ArrayList<>();

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_new")
    @Builder.Default
    private Boolean isNew = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}

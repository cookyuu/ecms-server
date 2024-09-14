package com.cookyuu.ecms_server.domain.product.entity;

import com.cookyuu.ecms_server.global.entity.BaseTimeEntity;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "ecms_category")
public class Category extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Builder
    public Category(String name, Category parent) {
        this.name = name;
        this.parent = parent;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> subCategories = new ArrayList<>();

    public static Category of(String name, Category parentCategory) {
        return  Category.builder().name(name).parent(parentCategory).build();
    }
}

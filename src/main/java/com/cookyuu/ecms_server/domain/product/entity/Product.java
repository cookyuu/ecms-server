package com.cookyuu.ecms_server.domain.product.entity;

import com.cookyuu.ecms_server.domain.cart.entity.CartItem;
import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.domain.order.entity.OrderLine;
import com.cookyuu.ecms_server.domain.review.entity.Review;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import com.cookyuu.ecms_server.global.entity.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ecms_product")
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Integer price;
    private Integer stockQuantity;

    @ColumnDefault("false")
    @Column(columnDefinition = "TINYINT(1)")
    private boolean isDeleted;

    @Column(name = "deleted_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-mm-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Category category;
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Seller seller;

    @OneToMany(mappedBy = "product")
    private List<OrderLine> orderLines = new ArrayList<>();
    @OneToMany(mappedBy = "product")
    private List<Review> reviews = new ArrayList<>();
    @OneToMany(mappedBy = "product")
    private List<CartItem> cartItems = new ArrayList<>();

    public static Product of(String name, String description, Integer price, Integer stockQuantity, Category category, Seller seller) {
        return Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stockQuantity(stockQuantity)
                .category(category)
                .seller(seller)
                .build();
    }

    public void updateInfo(String name, String description, Integer price, Integer stockQuantity, Category category) {
        this.name = StringUtils.isEmpty(name) ? this.name : name;
        this.description = StringUtils.isEmpty(description) ? this.description : description;
        this.price = price==null ? this.price : price;
        this.stockQuantity = stockQuantity==null ? this.stockQuantity : stockQuantity;
        this.category = category==null ? this.category : category;

    }

    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}

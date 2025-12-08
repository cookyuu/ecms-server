package com.cookyuu.ecms_server.domain.product.entity;

import com.cookyuu.ecms_server.domain.cart.entity.CartItem;
import com.cookyuu.ecms_server.domain.order.entity.OrderLine;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.domain.BaseTimeEntity;
import com.cookyuu.ecms_server.common.exception.BusinessException;
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
@Table(
        name = "ecms_product",
        indexes = {
                @Index(name = "ecms_product_search_idx_1", columnList = "name"),
                @Index(name = "ecms_product_search_idx_2", columnList = "seller_id"),
                @Index(name = "ecms_product_search_idx_3", columnList = "category_id"),
                @Index(name = "ecms_product_search_idx_4", columnList = "isDeleted")
        }
)
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "hit_count", nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private Integer hitCount = 0;

    @ColumnDefault("false")
    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1)")
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_category"))
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_seller"))
    private Seller seller;

    @OneToMany(mappedBy = "product")
    @Builder.Default
    private List<OrderLine> orderLines = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    @Builder.Default
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

    public void subQuantity(int quantity) {
        if (quantity < 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "차감할 수량은 0보다 커야 합니다.");
        }
        if (this.stockQuantity < quantity) {
            throw new BusinessException(ResultCode.PRODUCT_SOLD_OUT,
                "재고가 부족합니다. 현재 재고: " + this.stockQuantity + ", 요청 수량: " + quantity);
        }
        this.stockQuantity -= quantity;
    }

    public void addQuantity(int quantity) {
        if (quantity < 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "추가할 수량은 0보다 커야 합니다.");
        }
        this.stockQuantity += quantity;
    }

    public void validateNotDeleted() {
        if (isDeleted) {
            throw new BusinessException(ResultCode.ALREADY_DELETED_PRODUCT, "이미 삭제된 상품입니다. productId : " + id);
        }
    }

    public void applyHitCount(int hitCount) {
        this.hitCount+=hitCount;
    }
}

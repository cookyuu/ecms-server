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

/**
 * 상품 엔티티
 *
 * - 카테고리(Category) N:1 관계
 * - 판매자(Seller) N:1 관계
 * - Soft Delete 방식으로 삭제된 상품도 이력 유지
 */
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

    /**
     * 상품명
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * 상품 설명
     */
    @Column(length = 2000)
    private String description;

    /**
     * 상품 가격
     */
    @Column(nullable = false)
    private Integer price;

    /**
     * 재고 수량
     */
    @Column(nullable = false)
    private Integer stockQuantity;

    /**
     * 조회수
     */
    @Column(name = "hit_count", nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private Integer hitCount = 0;

    /**
     * Soft Delete 플래그
     */
    @ColumnDefault("false")
    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1)")
    @Builder.Default
    private boolean isDeleted = false;

    /**
     * 삭제 시각
     */
    @Column(name = "deleted_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime deletedAt;

    /**
     * 카테고리 (필수)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_category"))
    private Category category;

    /**
     * 판매자 (필수)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_seller"))
    private Seller seller;

    /**
     * 주문 라인 목록 (읽기 전용)
     * - 양방향 관계는 유지하되, 조회 최적화를 위해 명시
     * - 삭제된 상품의 경우에도 주문 이력 유지 필요
     */
    @OneToMany(mappedBy = "product")
    @Builder.Default
    private List<OrderLine> orderLines = new ArrayList<>();

    /**
     * 장바구니 아이템 목록 (읽기 전용)
     */
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
        this.stockQuantity -= quantity;
    }

    public void addQuantity(int quantity) {
        this.stockQuantity += quantity;
    }

    public void isDeleted() {
        if (isDeleted) {
            throw new BusinessException(ResultCode.ALREADY_DELETED_PRODUCT, "이미 삭제된 상품입니다. productId : " + id);
        }
    }

    public void applyHitCount(int hitCount) {
        this.hitCount+=hitCount;
    }
}

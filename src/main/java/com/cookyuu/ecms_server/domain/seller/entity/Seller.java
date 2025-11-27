package com.cookyuu.ecms_server.domain.seller.entity;

import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.domain.seller.dto.UpdateSellerDto;
import com.cookyuu.ecms_server.common.domain.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 판매자 엔티티
 *
 * - 상품(Product) 1:N 관계
 * - Soft Delete 방식으로 삭제된 판매자도 이력 유지
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "ecms_seller",
        indexes = {
                @Index(name = "ecms_seller_search_idx_1", columnList = "name"),
                @Index(name = "ecms_seller_search_idx_2", columnList = "loginId", unique = true),
                @Index(name = "ecms_seller_search_idx_3", columnList = "businessNumber", unique = true)
        }
)
public class Seller extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 판매자명
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 사업자명
     */
    @Column(nullable = false, length = 100)
    private String businessName;

    /**
     * 사업자 번호 (고유)
     */
    @Column(nullable = false, unique = true, length = 20)
    private String businessNumber;

    /**
     * 사업장 주소
     */
    @Column(nullable = false, length = 200)
    private String businessAddress;

    /**
     * 사업장 연락처
     */
    @Column(nullable = false, length = 20)
    private String businessContactTelNum;

    /**
     * 사업장 이메일
     */
    @Column(nullable = false, length = 100)
    private String businessContactEmail;

    /**
     * 로그인 ID (고유)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    /**
     * 비밀번호 (암호화됨)
     */
    @Column(nullable = false, length = 200)
    private String password;

    /**
     * 권한
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleType role;

    /**
     * Soft Delete 플래그
     */
    @ColumnDefault("false")
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    @Builder.Default
    private boolean isDeleted = false;

    /**
     * 삭제 시각
     */
    @Column
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime deletedAt;

    /**
     * 상품 목록 (읽기 전용)
     */
    @OneToMany(mappedBy = "seller")
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    public void updateInfo(UpdateSellerDto.Request sellerInfo) {
        this.name = StringUtils.isEmpty(sellerInfo.getName()) ? this.name : sellerInfo.getName();
        this.businessName = StringUtils.isEmpty(sellerInfo.getBusinessName()) ? this.businessName : sellerInfo.getBusinessName();
        this.businessAddress = StringUtils.isEmpty(sellerInfo.getBusinessAddress()) ? this.businessAddress : sellerInfo.getBusinessAddress();
        this.businessContactTelNum = StringUtils.isEmpty(sellerInfo.getBusinessContactTelNum()) ? this.businessContactTelNum : sellerInfo.getBusinessContactTelNum();
        this.businessContactEmail = StringUtils.isEmpty(sellerInfo.getBusinessContactEmail()) ? this.businessContactEmail : sellerInfo.getBusinessContactEmail();
    }

    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}

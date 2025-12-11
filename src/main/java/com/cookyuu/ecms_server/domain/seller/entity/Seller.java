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

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 100)
    private String businessName;

    @Column(nullable = false, unique = true, length = 20)
    private String businessNumber;

    @Column(nullable = false, length = 200)
    private String businessAddress;

    @Column(nullable = false, length = 20)
    private String businessContactTelNum;

    @Column(nullable = false, length = 100)
    private String businessContactEmail;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false, length = 200)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleType role;

    @ColumnDefault("false")
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    @Builder.Default
    private boolean isDeleted = false;

    @Column
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime deletedAt;

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

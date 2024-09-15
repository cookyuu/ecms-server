package com.cookyuu.ecms_server.domain.seller.entity;

import com.cookyuu.ecms_server.domain.member.entity.RoleType;
import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.domain.seller.dto.RegisterSellerDto;
import com.cookyuu.ecms_server.domain.seller.dto.UpdateSellerDto;
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ecms_seller")
public class Seller extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String businessName;
    private String businessNumber;
    private String businessAddress;
    private String businessContactTelNum;
    private String businessContactEmail;

    private String loginId;
    private String password;
    @Enumerated(EnumType.STRING)
    private RoleType role;

    @OneToMany(mappedBy = "seller")
    private List<Product> products = new ArrayList<>();
    @OneToMany(mappedBy = "seller")
    private List<Order> orders = new ArrayList<>();

    public void updateInfo(UpdateSellerDto.Request sellerInfo) {
        this.name = StringUtils.isEmpty(sellerInfo.getName()) ? this.name : sellerInfo.getName();
        this.businessName = StringUtils.isEmpty(sellerInfo.getBusinessName()) ? this.businessName : sellerInfo.getBusinessName();
        this.businessAddress = StringUtils.isEmpty(sellerInfo.getBusinessAddress()) ? this.businessAddress : sellerInfo.getBusinessAddress();
        this.businessContactTelNum = StringUtils.isEmpty(sellerInfo.getBusinessContactTelNum()) ? this.businessContactTelNum : sellerInfo.getBusinessContactTelNum();
        this.businessContactEmail = StringUtils.isEmpty(sellerInfo.getBusinessContactEmail()) ? this.businessContactEmail : sellerInfo.getBusinessContactEmail();
    }
}

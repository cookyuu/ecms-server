package com.cookyuu.ecms_server.domain.seller.entity;

import com.cookyuu.ecms_server.domain.member.entity.RoleType;
import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.domain.product.entity.Product;
import com.cookyuu.ecms_server.global.entity.BaseTimeEntity;
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

    public static Seller of(String loginId, String password, String name, String businessName, String businessNumber, String businessAddress,
                             String businessContactTelNum, String businessContactEmail) {
        return Seller.builder()
                .loginId(loginId)
                .password(password)
                .name(name)
                .businessName(businessName)
                .businessNumber(businessNumber)
                .businessAddress(businessAddress)
                .businessContactTelNum(businessContactTelNum)
                .businessContactEmail(businessContactEmail)
                .role(RoleType.SELLER)
                .build();
    }
}

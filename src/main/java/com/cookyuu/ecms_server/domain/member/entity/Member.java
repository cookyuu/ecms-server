package com.cookyuu.ecms_server.domain.member.entity;

import com.cookyuu.ecms_server.domain.cart.entity.Cart;
import com.cookyuu.ecms_server.domain.coupon.entity.IssueCoupon;
import com.cookyuu.ecms_server.domain.order.entity.Order;
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
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "ecms_member",
        indexes = {
                @Index(name = "ecms_member_search_idx_1", columnList = "loginId", unique = true)
        }
)
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String loginId;
    private String password;
    private String phoneNumber;
    private String address;
    @Enumerated(EnumType.STRING)
    private RoleType role;

    @OneToMany(mappedBy = "buyer")
    private List<Order> orders = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    private List<Cart> carts = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    private List<IssueCoupon> issueCoupons = new ArrayList<>();

    @Builder
    public Member(Long id, String name, String email, String loginId, String password, String phoneNumber, String address, RoleType role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.loginId = loginId;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
    }

    public boolean compareMemberId(Long memberId) {
        return this.id.equals(memberId);
    }

    public static Member of(String name, String email, String loginId, String password, String phoneNumber, String address) {
        return Member.builder()
                .name(name)
                .email(email)
                .loginId(loginId)
                .password(password)
                .phoneNumber(phoneNumber)
                .address(address)
                .role(RoleType.USER)
                .build();
    }

    public void updateRole(RoleType roleType) {
        this.role = roleType;
    }
}



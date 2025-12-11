package com.cookyuu.ecms_server.domain.member.entity;

import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import com.cookyuu.ecms_server.domain.cart.entity.Cart;
import com.cookyuu.ecms_server.domain.coupon.entity.IssueCoupon;
import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 회원 엔티티
 *
 * - 주문(Order) 1:N 관계
 * - 장바구니(Cart) 1:N 관계
 * - 발급쿠폰(IssueCoupon) 1:N 관계
 */
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "ecms_member",
        indexes = {
                @Index(name = "ecms_member_search_idx_1", columnList = "loginId", unique = true),
                @Index(name = "ecms_member_search_idx_2", columnList = "email")
        }
)
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 회원명
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 이메일
     */
    @Column(nullable = false, length = 100)
    private String email;

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
     * 전화번호
     */
    @Column(length = 20)
    private String phoneNumber;

    /**
     * 주소
     */
    @Column(length = 200)
    private String address;

    /**
     * 권한
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleType role;

    /**
     * 주문 목록 (읽기 전용)
     */
    @OneToMany(mappedBy = "buyer")
    private List<Order> orders = new ArrayList<>();

    /**
     * 장바구니 목록 (읽기 전용)
     */
    @OneToMany(mappedBy = "member")
    private List<Cart> carts = new ArrayList<>();

    /**
     * 발급 쿠폰 목록 (읽기 전용)
     */
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



package com.cookyuu.ecms_server.domain.member.entity;

import com.cookyuu.ecms_server.domain.cart.entity.Cart;
import com.cookyuu.ecms_server.domain.order.entity.Order;
import com.cookyuu.ecms_server.domain.review.entity.Review;
import com.cookyuu.ecms_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ecms_member")
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String userId;
    private String password;
    private String phoneNumber;
    private String address;
    @Enumerated(EnumType.STRING)
    private RoleType role;

    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    private List<Review> reviews = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    private List<Cart> carts = new ArrayList<>();

    public static Member of(String name, String email, String userId, String password, String phoneNumber, String address) {
        Member member = new Member();
        member.name = name;
        member.email = email;
        member.userId = userId;
        member.password = password;
        member.phoneNumber = phoneNumber;
        member.address = address;
        member.role = RoleType.USER;
        return member;
    }
}



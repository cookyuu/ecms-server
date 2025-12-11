package com.cookyuu.ecms_server.domain.coupon.entity;

import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "ecms_issue_coupon",
        indexes = {
                @Index(name = "ecms_issue_coupon_idx_1", columnList = "member_id"),
                @Index(name = "ecms_issue_coupon_idx_2", columnList = "coupon_id")
        }
)
public class IssueCoupon extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Builder.Default
    private boolean isUseAble = true;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    private LocalDateTime couponUsedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false, foreignKey = @ForeignKey(name = "fk_issue_coupon_coupon"))
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_issue_coupon_member"))
    private Member member;
}

package com.cookyuu.ecms_server.domain.coupon.entity;

import com.cookyuu.ecms_server.domain.member.entity.Member;
import com.cookyuu.ecms_server.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 발급 쿠폰 엔티티
 *
 * - 쿠폰(Coupon) N:1 관계
 * - 회원(Member) N:1 관계
 */
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

    /**
     * 사용 가능 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean isUseAble = true;

    /**
     * 쿠폰 만료일
     */
    @Column(nullable = false)
    private LocalDateTime expiredAt;

    /**
     * 쿠폰 사용 일시
     */
    private LocalDateTime couponUsedAt;

    /**
     * 쿠폰 (필수)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false, foreignKey = @ForeignKey(name = "fk_issue_coupon_coupon"))
    private Coupon coupon;

    /**
     * 회원 (필수)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_issue_coupon_member"))
    private Member member;
}

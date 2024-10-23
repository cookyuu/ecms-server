package com.cookyuu.ecms_server.domain.member.repository;

import com.cookyuu.ecms_server.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberSearchRepository{
    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByLoginId(String loginId);

    Optional<Object> findByLoginId(String loginId);
}

package com.cookyuu.ecms_server.common.security.service;

import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @InjectMocks
    private AuthorizationService authorizationService;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @DisplayName("요청 사용자 ID와 리소스 소유자 ID가 일치할 때 소유권 검증을 하면 성공한다")
    @Test
    void givenMatchingIds_whenValidateResourceOwnership_thenSuccess() {
        // Given
        Long requestUserId = 1L;
        Long resourceOwnerId = 1L;
        ResultCode errorCode = ResultCode.ORDER_BUYER_UNMATCHED;

        // When & Then
        authorizationService.validateResourceOwnership(requestUserId, resourceOwnerId, errorCode);
    }

    @DisplayName("요청 사용자 ID와 리소스 소유자 ID가 일치하지 않을 때 소유권 검증을 하면 예외가 발생한다")
    @Test
    void givenNonMatchingIds_whenValidateResourceOwnership_thenThrowException() {
        // Given
        Long requestUserId = 1L;
        Long resourceOwnerId = 2L;
        ResultCode errorCode = ResultCode.ORDER_BUYER_UNMATCHED;

        // When & Then
        assertThatThrownBy(() -> authorizationService.validateResourceOwnership(requestUserId, resourceOwnerId, errorCode))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", errorCode);
    }

    @DisplayName("USER 권한을 가진 사용자가 주어질 때 역할을 조회하면 USER가 반환된다")
    @Test
    void givenUserRole_whenGetUserRole_thenReturnUser() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.USER);

        // When
        RoleType role = authorizationService.getUserRole(user);

        // Then
        assertThat(role).isEqualTo(RoleType.USER);
    }

    @DisplayName("SELLER 권한을 가진 사용자가 주어질 때 역할을 조회하면 SELLER가 반환된다")
    @Test
    void givenSellerRole_whenGetUserRole_thenReturnSeller() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.SELLER);

        // When
        RoleType role = authorizationService.getUserRole(user);

        // Then
        assertThat(role).isEqualTo(RoleType.SELLER);
    }

    @DisplayName("ADMIN 권한을 가진 사용자가 주어질 때 역할을 조회하면 ADMIN이 반환된다")
    @Test
    void givenAdminRole_whenGetUserRole_thenReturnAdmin() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.ADMIN);

        // When
        RoleType role = authorizationService.getUserRole(user);

        // Then
        assertThat(role).isEqualTo(RoleType.ADMIN);
    }

    @DisplayName("USER 권한을 가진 사용자가 USER 권한을 확인할 때 true가 반환된다")
    @Test
    void givenUserRole_whenCheckUserRole_thenReturnTrue() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.USER);

        // When
        boolean hasRole = authorizationService.hasRole(user, RoleType.USER);

        // Then
        assertThat(hasRole).isTrue();
    }

    @DisplayName("USER 권한을 가진 사용자가 ADMIN 권한을 확인할 때 false가 반환된다")
    @Test
    void givenUserRole_whenCheckAdminRole_thenReturnFalse() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.USER);

        // When
        boolean hasRole = authorizationService.hasRole(user, RoleType.ADMIN);

        // Then
        assertThat(hasRole).isFalse();
    }

    @DisplayName("USER 권한을 가진 사용자가 여러 권한 중 USER를 포함할 때 true가 반환된다")
    @Test
    void givenUserRole_whenCheckMultipleRolesIncludingUser_thenReturnTrue() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.USER);

        // When
        boolean hasRole = authorizationService.hasRole(user, RoleType.USER, RoleType.ADMIN);

        // Then
        assertThat(hasRole).isTrue();
    }

    @DisplayName("허용된 권한을 가진 사용자가 주어질 때 권한 필수 검증을 하면 성공한다")
    @Test
    void givenAllowedRole_whenRequireRole_thenSuccess() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.ADMIN);
        ResultCode errorCode = ResultCode.PAYMENT_INACCESSIBLE_DETAIL;

        // When & Then
        authorizationService.requireRole(user, errorCode, RoleType.ADMIN, RoleType.SELLER);
    }

    @DisplayName("허용되지 않은 권한을 가진 사용자가 주어질 때 권한 필수 검증을 하면 예외가 발생한다")
    @Test
    void givenUnallowedRole_whenRequireRole_thenThrowException() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.USER);
        ResultCode errorCode = ResultCode.PAYMENT_INACCESSIBLE_DETAIL;

        // When & Then
        assertThatThrownBy(() -> authorizationService.requireRole(user, errorCode, RoleType.ADMIN, RoleType.SELLER))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", errorCode);
    }

    @DisplayName("ADMIN 권한으로 리소스 목록이 주어질 때 사용자 접근을 검증하면 성공한다")
    @Test
    void givenAdminRole_whenValidateUserAccess_thenSuccess() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.ADMIN);
        List<TestResource> resources = Arrays.asList(
            new TestResource(2L),
            new TestResource(3L)
        );

        // When & Then
        authorizationService.validateUserAccess(
            user,
            resources,
            1L,
            resource -> resource.getOwnerId().equals(1L),
            ResultCode.PAYMENT_INACCESSIBLE_DETAIL
        );
    }

    @DisplayName("USER 권한으로 자신의 리소스가 포함된 목록이 주어질 때 사용자 접근을 검증하면 성공한다")
    @Test
    void givenUserRoleWithOwnResource_whenValidateUserAccess_thenSuccess() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.USER);
        List<TestResource> resources = Arrays.asList(
            new TestResource(1L),
            new TestResource(2L)
        );

        // When & Then
        authorizationService.validateUserAccess(
            user,
            resources,
            1L,
            resource -> resource.getOwnerId().equals(1L),
            ResultCode.PAYMENT_INACCESSIBLE_DETAIL
        );
    }

    @DisplayName("USER 권한으로 자신의 리소스가 없는 목록이 주어질 때 사용자 접근을 검증하면 예외가 발생한다")
    @Test
    void givenUserRoleWithoutOwnResource_whenValidateUserAccess_thenThrowException() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.USER);
        List<TestResource> resources = Arrays.asList(
            new TestResource(2L),
            new TestResource(3L)
        );

        // When & Then
        assertThatThrownBy(() -> authorizationService.validateUserAccess(
            user,
            resources,
            1L,
            resource -> resource.getOwnerId().equals(1L),
            ResultCode.PAYMENT_INACCESSIBLE_DETAIL
        ))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("resultCode", ResultCode.PAYMENT_INACCESSIBLE_DETAIL);
    }

    @DisplayName("ADMIN 권한으로 리소스 목록이 주어질 때 역할별 필터링을 하면 모든 리소스가 반환된다")
    @Test
    void givenAdminRole_whenFilterResourcesByRole_thenReturnAllResources() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.ADMIN);
        List<TestResource> resources = Arrays.asList(
            new TestResource(1L),
            new TestResource(2L),
            new TestResource(3L)
        );

        // When
        List<TestResource> filtered = authorizationService.filterResourcesByRole(
            user,
            resources,
            1L,
            resource -> resource.getOwnerId().equals(1L),
            ResultCode.PAYMENT_INACCESSIBLE_DETAIL
        );

        // Then
        assertThat(filtered).hasSize(3);
    }

    @DisplayName("SELLER 권한으로 자신의 리소스가 포함된 목록이 주어질 때 역할별 필터링을 하면 자신의 리소스만 반환된다")
    @Test
    void givenSellerRoleWithOwnResources_whenFilterResourcesByRole_thenReturnOwnResources() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.SELLER);
        List<TestResource> resources = Arrays.asList(
            new TestResource(1L),
            new TestResource(2L),
            new TestResource(1L)
        );

        // When
        List<TestResource> filtered = authorizationService.filterResourcesByRole(
            user,
            resources,
            1L,
            resource -> resource.getOwnerId().equals(1L),
            ResultCode.PAYMENT_INACCESSIBLE_DETAIL
        );

        // Then
        assertThat(filtered).hasSize(2);
        assertThat(filtered).allMatch(resource -> resource.getOwnerId().equals(1L));
    }

    @DisplayName("SELLER 권한으로 자신의 리소스가 없는 목록이 주어질 때 역할별 필터링을 하면 예외가 발생한다")
    @Test
    void givenSellerRoleWithoutOwnResources_whenFilterResourcesByRole_thenThrowException() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.SELLER);
        List<TestResource> resources = Arrays.asList(
            new TestResource(2L),
            new TestResource(3L)
        );

        // When & Then
        assertThatThrownBy(() -> authorizationService.filterResourcesByRole(
            user,
            resources,
            1L,
            resource -> resource.getOwnerId().equals(1L),
            ResultCode.PAYMENT_INACCESSIBLE_DETAIL
        ))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("resultCode", ResultCode.PAYMENT_INACCESSIBLE_DETAIL);
    }

    @DisplayName("요청 사용자 ID와 리소스 소유자 ID가 일치할 때 소유권을 확인하면 true가 반환된다")
    @Test
    void givenMatchingIds_whenCheckOwnership_thenReturnTrue() {
        // Given
        Long requestUserId = 1L;
        Long resourceOwnerId = 1L;

        // When
        boolean isOwner = authorizationService.checkOwnership(requestUserId, resourceOwnerId);

        // Then
        assertThat(isOwner).isTrue();
    }

    @DisplayName("요청 사용자 ID와 리소스 소유자 ID가 일치하지 않을 때 소유권을 확인하면 false가 반환된다")
    @Test
    void givenNonMatchingIds_whenCheckOwnership_thenReturnFalse() {
        // Given
        Long requestUserId = 1L;
        Long resourceOwnerId = 2L;

        // When
        boolean isOwner = authorizationService.checkOwnership(requestUserId, resourceOwnerId);

        // Then
        assertThat(isOwner).isFalse();
    }

    @DisplayName("USER 권한으로 자신이 구매자인 주문이 주어질 때 구매자 접근을 검증하면 성공한다")
    @Test
    void givenUserRoleWithOwnOrder_whenValidateOrderBuyerAccess_thenSuccess() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.USER);
        Long buyerId = 1L;

        // When & Then
        authorizationService.validateOrderBuyerAccess(user, buyerId);
    }

    @DisplayName("USER 권한으로 자신이 구매자가 아닌 주문이 주어질 때 구매자 접근을 검증하면 예외가 발생한다")
    @Test
    void givenUserRoleWithOtherUserOrder_whenValidateOrderBuyerAccess_thenThrowException() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.USER);
        Long buyerId = 2L;

        // When & Then
        assertThatThrownBy(() -> authorizationService.validateOrderBuyerAccess(user, buyerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.ORDER_BUYER_UNMATCHED);
    }

    @DisplayName("SELLER 권한으로 자신이 판매자가 아닌 주문이 주어질 때 구매자 접근을 검증하면 성공한다")
    @Test
    void givenSellerRole_whenValidateOrderBuyerAccess_thenSuccess() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.SELLER);
        Long buyerId = 2L;

        // When & Then
        authorizationService.validateOrderBuyerAccess(user, buyerId);
    }

    @DisplayName("ADMIN 권한으로 주문이 주어질 때 구매자 접근을 검증하면 성공한다")
    @Test
    void givenAdminRole_whenValidateOrderBuyerAccess_thenSuccess() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.ADMIN);
        Long buyerId = 2L;

        // When & Then
        authorizationService.validateOrderBuyerAccess(user, buyerId);
    }

    @DisplayName("SELLER 권한으로 자신이 판매자인 주문이 주어질 때 판매자 접근을 검증하면 성공한다")
    @Test
    void givenSellerRoleWithOwnProduct_whenValidateOrderSellerAccess_thenSuccess() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.SELLER);
        List<Long> sellerIds = Arrays.asList(1L, 2L, 3L);

        // When & Then
        authorizationService.validateOrderSellerAccess(user, sellerIds);
    }

    @DisplayName("SELLER 권한으로 자신이 판매자가 아닌 주문이 주어질 때 판매자 접근을 검증하면 예외가 발생한다")
    @Test
    void givenSellerRoleWithoutOwnProduct_whenValidateOrderSellerAccess_thenThrowException() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.SELLER);
        List<Long> sellerIds = Arrays.asList(2L, 3L, 4L);

        // When & Then
        assertThatThrownBy(() -> authorizationService.validateOrderSellerAccess(user, sellerIds))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.ORDER_SELLER_UNMATCHED);
    }

    @DisplayName("USER 권한으로 주문이 주어질 때 판매자 접근을 검증하면 성공한다")
    @Test
    void givenUserRole_whenValidateOrderSellerAccess_thenSuccess() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.USER);
        List<Long> sellerIds = Arrays.asList(2L, 3L);

        // When & Then
        authorizationService.validateOrderSellerAccess(user, sellerIds);
    }

    @DisplayName("ADMIN 권한으로 주문이 주어질 때 판매자 접근을 검증하면 성공한다")
    @Test
    void givenAdminRole_whenValidateOrderSellerAccess_thenSuccess() {
        // Given
        UserDetails user = createUserDetails("1", RoleType.ADMIN);
        List<Long> sellerIds = Arrays.asList(2L, 3L);

        // When & Then
        authorizationService.validateOrderSellerAccess(user, sellerIds);
    }

    // Helper methods
    private UserDetails createUserDetails(String userId, RoleType role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        return new User(userId, "password", authorities);
    }

    // Test resource class
    private static class TestResource {
        private final Long ownerId;

        public TestResource(Long ownerId) {
            this.ownerId = ownerId;
        }

        public Long getOwnerId() {
            return ownerId;
        }
    }
}

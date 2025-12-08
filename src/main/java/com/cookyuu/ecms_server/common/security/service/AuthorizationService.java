package com.cookyuu.ecms_server.common.security.service;

import com.cookyuu.ecms_server.domain.member.enums.RoleType;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Slf4j
@Service
public class AuthorizationService {

    public void validateResourceOwnership(Long requestUserId, Long resourceOwnerId, ResultCode errorCode) {
        log.atDebug()
            .addKeyValue("operation", "validateResourceOwnership")
            .addKeyValue("request_user_id", requestUserId)
            .addKeyValue("resource_owner_id", resourceOwnerId)
            .log("Validating resource ownership");

        if (!resourceOwnerId.equals(requestUserId)) {
            log.atWarn()
                .addKeyValue(EVENT, AUTHORIZATION_ERROR)
                .addKeyValue("request_user_id", requestUserId)
                .addKeyValue("resource_owner_id", resourceOwnerId)
                .addKeyValue(ERROR_CODE, errorCode.getCode())
                .log("Resource ownership validation failed");
            throw new BusinessException(errorCode);
        }

        log.atDebug()
            .addKeyValue("operation", "validateResourceOwnership")
            .addKeyValue("request_user_id", requestUserId)
            .log("Resource ownership validated successfully");
    }

    public RoleType getUserRole(UserDetails user) {
        String jwtRole = JwtUtils.getRoleFromUserDetails(user);
        String roleString = jwtRole.replace("ROLE_", "");

        log.atDebug()
            .addKeyValue("operation", "getUserRole")
            .addKeyValue(USER_ROLE, roleString)
            .log("Retrieved user role");

        return RoleType.valueOf(roleString);
    }

    public boolean hasRole(UserDetails user, RoleType... allowedRoles) {
        RoleType userRole = getUserRole(user);
        boolean hasRole = Arrays.asList(allowedRoles).contains(userRole);

        log.atDebug()
            .addKeyValue("operation", "hasRole")
            .addKeyValue(USER_ROLE, userRole.name())
            .addKeyValue("allowed_roles", Arrays.toString(allowedRoles))
            .addKeyValue("has_role", hasRole)
            .log("Role check performed");

        return hasRole;
    }

    public void requireRole(UserDetails user, ResultCode errorCode, RoleType... allowedRoles) {
        if (!hasRole(user, allowedRoles)) {
            RoleType userRole = getUserRole(user);
            log.atWarn()
                .addKeyValue(EVENT, AUTHORIZATION_ERROR)
                .addKeyValue(USER_ROLE, userRole.name())
                .addKeyValue("allowed_roles", Arrays.toString(allowedRoles))
                .addKeyValue(ERROR_CODE, errorCode.getCode())
                .log("Role requirement validation failed");
            throw new BusinessException(errorCode);
        }
    }

    public <T> void validateUserAccess(UserDetails user, List<T> resources,
                                        Long requestUserId,
                                        Predicate<T> accessChecker,
                                        ResultCode errorCode) {
        RoleType userRole = getUserRole(user);

        log.atDebug()
            .addKeyValue("operation", "validateUserAccess")
            .addKeyValue(USER_ROLE, userRole.name())
            .addKeyValue("request_user_id", requestUserId)
            .addKeyValue("resource_count", resources.size())
            .log("Validating user access");

        if (userRole == RoleType.ADMIN) {
            log.atDebug()
                .addKeyValue("operation", "validateUserAccess")
                .addKeyValue(USER_ROLE, userRole.name())
                .log("Admin access granted - full access");
            return;
        }

        boolean hasAccess = resources.stream().anyMatch(accessChecker);
        if (!hasAccess) {
            log.atWarn()
                .addKeyValue(EVENT, AUTHORIZATION_ERROR)
                .addKeyValue(USER_ROLE, userRole.name())
                .addKeyValue("request_user_id", requestUserId)
                .addKeyValue(ERROR_CODE, errorCode.getCode())
                .log("User access validation failed - no matching resources");
            throw new BusinessException(errorCode);
        }

        log.atDebug()
            .addKeyValue("operation", "validateUserAccess")
            .addKeyValue(USER_ROLE, userRole.name())
            .addKeyValue("request_user_id", requestUserId)
            .log("User access validated successfully");
    }

    public <T> List<T> filterResourcesByRole(UserDetails user, List<T> resources,
                                              Long requestUserId,
                                              Predicate<T> ownershipFilter,
                                              ResultCode errorCode) {
        RoleType userRole = getUserRole(user);

        log.atDebug()
            .addKeyValue("operation", "filterResourcesByRole")
            .addKeyValue(USER_ROLE, userRole.name())
            .addKeyValue("request_user_id", requestUserId)
            .addKeyValue("resource_count", resources.size())
            .log("Filtering resources by role");

        if (userRole == RoleType.ADMIN) {
            log.atDebug()
                .addKeyValue("operation", "filterResourcesByRole")
                .addKeyValue(USER_ROLE, userRole.name())
                .addKeyValue("filtered_count", resources.size())
                .log("Admin access - all resources returned");
            return resources;
        }

        List<T> filteredResources = resources.stream()
                .filter(ownershipFilter)
                .toList();

        if (filteredResources.isEmpty()) {
            log.atWarn()
                .addKeyValue(EVENT, AUTHORIZATION_ERROR)
                .addKeyValue(USER_ROLE, userRole.name())
                .addKeyValue("request_user_id", requestUserId)
                .addKeyValue(ERROR_CODE, errorCode.getCode())
                .log("Resource filtering resulted in empty list");
            throw new BusinessException(errorCode);
        }

        log.atDebug()
            .addKeyValue("operation", "filterResourcesByRole")
            .addKeyValue(USER_ROLE, userRole.name())
            .addKeyValue("original_count", resources.size())
            .addKeyValue("filtered_count", filteredResources.size())
            .log("Resources filtered successfully");

        return filteredResources;
    }

    public boolean checkOwnership(Long requestUserId, Long resourceOwnerId) {
        log.atDebug()
            .addKeyValue("operation", "checkOwnership")
            .addKeyValue("request_user_id", requestUserId)
            .addKeyValue("resource_owner_id", resourceOwnerId)
            .log("Checking ownership");

        boolean isOwner = resourceOwnerId.equals(requestUserId);

        log.atDebug()
            .addKeyValue("operation", "checkOwnership")
            .addKeyValue("is_owner", isOwner)
            .log("Ownership check completed");

        return isOwner;
    }

    public void validateOrderBuyerAccess(UserDetails user, Long buyerId) {
        RoleType userRole = getUserRole(user);
        Long requestUserId = Long.parseLong(user.getUsername());

        log.atDebug()
            .addKeyValue("operation", "validateOrderBuyerAccess")
            .addKeyValue(USER_ROLE, userRole.name())
            .addKeyValue("buyer_id", buyerId)
            .addKeyValue("request_user_id", requestUserId)
            .log("Validating order buyer access");

        if (userRole == RoleType.USER) {
            validateResourceOwnership(requestUserId, buyerId, ResultCode.ORDER_BUYER_UNMATCHED);
        }

        log.atDebug()
            .addKeyValue("operation", "validateOrderBuyerAccess")
            .addKeyValue(USER_ROLE, userRole.name())
            .log("Order buyer access validated");
    }

    public void validateOrderSellerAccess(UserDetails user, List<Long> sellerIds) {
        RoleType userRole = getUserRole(user);
        Long requestUserId = Long.parseLong(user.getUsername());

        log.atDebug()
            .addKeyValue("operation", "validateOrderSellerAccess")
            .addKeyValue(USER_ROLE, userRole.name())
            .addKeyValue("request_user_id", requestUserId)
            .addKeyValue("seller_count", sellerIds.size())
            .log("Validating order seller access");

        if (userRole == RoleType.SELLER) {
            boolean isSeller = sellerIds.stream().anyMatch(sellerId -> sellerId.equals(requestUserId));
            if (!isSeller) {
                log.atWarn()
                    .addKeyValue(EVENT, AUTHORIZATION_ERROR)
                    .addKeyValue(USER_ROLE, userRole.name())
                    .addKeyValue("request_user_id", requestUserId)
                    .addKeyValue(ERROR_CODE, ResultCode.ORDER_SELLER_UNMATCHED.getCode())
                    .log("Order seller access validation failed");
                throw new BusinessException(ResultCode.ORDER_SELLER_UNMATCHED);
            }
        }

        log.atDebug()
            .addKeyValue("operation", "validateOrderSellerAccess")
            .addKeyValue(USER_ROLE, userRole.name())
            .log("Order seller access validated");
    }
}

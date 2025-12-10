package com.cookyuu.ecms_server.domain.seller.service;

import com.cookyuu.ecms_server.domain.auth.dto.JWTUserInfo;
import com.cookyuu.ecms_server.domain.seller.dto.DeleteSellerDto;
import com.cookyuu.ecms_server.domain.seller.dto.RegisterSellerDto;
import com.cookyuu.ecms_server.domain.seller.dto.SellerDetailDto;
import com.cookyuu.ecms_server.domain.seller.dto.UpdateSellerDto;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import com.cookyuu.ecms_server.domain.seller.mapper.SellerRegistrationMapper;
import com.cookyuu.ecms_server.domain.seller.repository.SellerRepository;
import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.AuthenticationException;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.common.utils.AuthUtils;
import com.cookyuu.ecms_server.common.utils.UserUtils;
import com.cookyuu.ecms_server.common.utils.ValidateUtils;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.cookyuu.ecms_server.common.logging.LogEvents.*;
import static com.cookyuu.ecms_server.common.logging.LogFields.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepository sellerRepository;
    private final ValidateUtils validateUtils;
    private final AuthUtils authUtils;
    private final UserUtils userUtils;

    public Seller findSellerById(Long sellerId) {
        log.atDebug()
                .addKeyValue(SELLER_ID, sellerId)
                .log("Finding seller by ID");
        return sellerRepository.findById(sellerId).orElseThrow(() -> new BusinessException(ResultCode.SELLER_NOT_FOUND));
    }

    @Transactional
    public RegisterSellerDto.Response registerSeller (RegisterSellerDto.Request sellerInfo) {
        validateSellerPersonalInfo(sellerInfo.getLoginId(), sellerInfo.getBusinessNumber(), sellerInfo.getBusinessContactTelNum(), sellerInfo.getBusinessContactEmail());
        Seller registerSeller = SellerRegistrationMapper.toEntity(sellerInfo, validateAndEncryptPassword(sellerInfo.getPassword()));
        Seller seller = sellerRepository.save(registerSeller);
        log.atInfo()
                .addKeyValue(EVENT, SELLER_REGISTERED)
                .addKeyValue(SELLER_ID, seller.getId())
                .addKeyValue(LOGIN_ID, seller.getLoginId())
                .addKeyValue(BUSINESS_NUMBER, seller.getBusinessNumber())
                .log("Seller registered successfully");
        return SellerRegistrationMapper.toDto(seller);
    }

    @Transactional
    public void updateSellerInfo(UserDetails user, UpdateSellerDto.Request sellerInfo) {
        sellerInfo.chkAllNull();
        Seller seller = findSellerById(userUtils.getUserId(user));
        String reqPw = sellerInfo.getPassword();
        String jwtPw = user.getPassword();
        authUtils.checkPassword(jwtPw, reqPw);
        validateSellerPersonalInfo(null, null, sellerInfo.getBusinessContactTelNum(), sellerInfo.getBusinessContactEmail());
        seller.updateInfo(sellerInfo);
        log.atInfo()
                .addKeyValue(EVENT, SELLER_UPDATED)
                .addKeyValue(SELLER_ID, seller.getId())
                .addKeyValue(LOGIN_ID, seller.getLoginId())
                .log("Seller information updated successfully");
    }

    @Transactional
    public void deleteSeller(UserDetails user, DeleteSellerDto.Request sellerInfo) {
        if (!sellerInfo.getPassword().equalsIgnoreCase(sellerInfo.getConfirmPassword())) {
            throw new BusinessException(ResultCode.CONFIRM_PASSWORD_UNMATCHED);
        }
        Seller seller = findSellerById(userUtils.getUserId(user));
        authUtils.checkPassword(seller.getPassword(), sellerInfo.getPassword());
        seller.delete();
        log.atInfo()
                .addKeyValue(EVENT, SELLER_DELETED)
                .addKeyValue(SELLER_ID, seller.getId())
                .addKeyValue(LOGIN_ID, seller.getLoginId())
                .log("Seller account deleted");
    }

    @Transactional(readOnly = true)
    public SellerDetailDto getSellerDetail(Long reqUserId) {
        return sellerRepository.getSellerDetail(reqUserId);
    }

    private void validateSellerPersonalInfo(String loginId, String businessNumber, String telNum, String email) {
        if (StringUtils.isNotEmpty(loginId) && sellerRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ResultCode.VALID_LOGINID_DUPLICATE);
        }
        if (StringUtils.isNotEmpty(businessNumber)) {
            validateUtils.isAvailableBusinessNumber(businessNumber);
            if (sellerRepository.existsByBusinessNumber(businessNumber)) {
                throw new BusinessException(ResultCode.VALID_BUSINESSNUM_DUPLICATE);
            }
        }
        if (StringUtils.isNotEmpty(telNum)) {
            validateUtils.isAvailablePhoneNumberFormat(telNum);
        }
        if (StringUtils.isNotEmpty(email)) {
            validateUtils.isAvailableEmailFormat(email);
        }
        log.atDebug()
                .addKeyValue(LOGIN_ID, loginId)
                .addKeyValue(BUSINESS_NUMBER, businessNumber)
                .log("Seller validation completed");
    }

    protected String validateAndEncryptPassword(String password) {
        validateUtils.isAvailablePasswordFormat(password);
        return authUtils.encryptPassword(password);
    }

    public JWTUserInfo checkLoginCredentials(String loginId, String password) {
        Seller seller = sellerRepository.findByLoginId(loginId).orElseThrow(()->
                new AuthenticationException(ResultCode.SELLER_NOT_FOUND));
        authUtils.checkPassword(seller.getPassword(), password);
        JWTUserInfo userInfo = new JWTUserInfo();
        userInfo.of(seller);
        log.atInfo()
                .addKeyValue(EVENT, LOGIN_SUCCESS)
                .addKeyValue(SELLER_ID, seller.getId())
                .addKeyValue(LOGIN_ID, seller.getLoginId())
                .log("Seller login credentials verified");
        return userInfo;
    }
}

package com.cookyuu.ecms_server.domain.seller.service;

import com.cookyuu.ecms_server.domain.auth.dto.JWTUserInfo;
import com.cookyuu.ecms_server.domain.seller.dto.RegisterSellerDto;
import com.cookyuu.ecms_server.domain.seller.dto.UpdateSellerDto;
import com.cookyuu.ecms_server.domain.seller.entity.Seller;
import com.cookyuu.ecms_server.domain.seller.mapper.SellerRegistrationMapper;
import com.cookyuu.ecms_server.domain.seller.repository.SellerRepository;
import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.auth.UserLoginException;
import com.cookyuu.ecms_server.global.exception.domain.ECMSSellerException;
import com.cookyuu.ecms_server.global.utils.AuthUtils;
import com.cookyuu.ecms_server.global.utils.ValidateUtils;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepository sellerRepository;
    private final ValidateUtils validateUtils;
    private final AuthUtils authUtils;

    public Seller findSellerById(Long sellerId) {
        return sellerRepository.findById(sellerId).orElseThrow(ECMSSellerException::new);
    }

    @Transactional
    public RegisterSellerDto.Response registerSeller (RegisterSellerDto.Request sellerInfo) {
        validateSellerPersonalInfo(sellerInfo.getLoginId(), sellerInfo.getBusinessNumber(), sellerInfo.getBusinessContactTelNum(), sellerInfo.getBusinessContactEmail());
        Seller registerSeller = SellerRegistrationMapper.toEntity(sellerInfo, validateAndEncryptPassword(sellerInfo.getPassword()));
        Seller seller = sellerRepository.save(registerSeller);
        log.info("[RegisterSeller] Register seller, OK!");
        return SellerRegistrationMapper.toDto(seller);
    }

    @Transactional
    public void updateSellerInfo(UserDetails user, UpdateSellerDto.Request sellerInfo) {
        sellerInfo.chkAllNull();
        Seller seller = findSellerById(Long.parseLong(user.getUsername()));
        String reqPw = sellerInfo.getPassword();
        String jwtPw = user.getPassword();
        authUtils.checkPassword(jwtPw, reqPw);
        validateSellerPersonalInfo(null, null, sellerInfo.getBusinessContactTelNum(), sellerInfo.getBusinessContactEmail());
        seller.updateInfo(sellerInfo);
        log.info("[UpdateSellerInfo] Update seller personal info OK!, SellerId : {}", seller.getId());
    }

    private void validateSellerPersonalInfo(String loginId, String businessNumber, String telNum, String email) {
        if (StringUtils.isNotEmpty(loginId) && sellerRepository.existsByLoginId(loginId)) {
            throw new ECMSSellerException(ResultCode.VALID_LOGINID_DUPLICATE);
        }
        if (StringUtils.isNotEmpty(businessNumber)) {
            validateUtils.isAvailableBusinessNumber(businessNumber);
            if (sellerRepository.existsByBusinessNumber(businessNumber)) {
                throw new ECMSSellerException(ResultCode.VALID_BUSINESSNUM_DUPLICATE);
            }
        }
        if (StringUtils.isNotEmpty(telNum)) {
            validateUtils.isAvailablePhoneNumberFormat(telNum);
        }
        if (StringUtils.isNotEmpty(email)) {
            validateUtils.isAvailableEmailFormat(email);
        }
        log.info("[RegisterSeller] Validate seller info OK!");
    }

    protected String validateAndEncryptPassword(String password) {
        validateUtils.isAvailablePasswordFormat(password);
        return authUtils.encryptPassword(password);
    }

    public JWTUserInfo checkLoginCredentials(String loginId, String password) {
        Seller seller = sellerRepository.findByLoginId(loginId).orElseThrow(()->
                new UserLoginException(ResultCode.MEMBER_NOT_FOUND));
        authUtils.checkPassword(seller.getPassword(), password);
        JWTUserInfo userInfo = new JWTUserInfo();
        userInfo.of(seller);
        log.info("[CheckLoginCredential] Check login credential OK!, Member loginId : {}", seller.getLoginId());
        return userInfo;
    }
}

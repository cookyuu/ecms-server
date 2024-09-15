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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        validateRegistrationSeller(sellerInfo);
        Seller registerSeller = SellerRegistrationMapper.toEntity(sellerInfo, validateAndEncryptPassword(sellerInfo.getPassword()));
        Seller seller = sellerRepository.save(registerSeller);
        log.info("[RegisterSeller] Register seller, OK!!");
        return SellerRegistrationMapper.toDto(seller);
    }

    @Transactional
    public void updateSellerInfo(UserDetails user, UpdateSellerDto.Request sellerInfo) {
        Seller seller = findSellerById(Long.parseLong(user.getUsername()));
        String reqPw = validateAndEncryptPassword(sellerInfo.getPassword());
        String jwtPw = user.getPassword();
        if (!reqPw.equals(jwtPw)) {
            throw new ECMSSellerException(ResultCode.AUTH_PASSWORD_UNMATCHED);
        }
        sellerInfo.chkAllNull();
        seller.updateInfo(sellerInfo);
        log.info("[UpdateSellerInfo] Update seller personal info OK!, SellerId : {}", seller.getId());
    }

    private void validateRegistrationSeller(RegisterSellerDto.Request sellerInfo){
        if (sellerRepository.existsByLoginId(sellerInfo.getLoginId())) {
            throw new ECMSSellerException(ResultCode.VALID_LOGINID_DUPLICATE);
        }
        if (sellerRepository.existsByBusinessNumber(sellerInfo.getBusinessNumber())) {
            throw new ECMSSellerException(ResultCode.VALID_BUSINESSNUM_DUPLICATE);
        }
        validateUtils.isAvailableBusinessNumber(sellerInfo.getBusinessNumber());
        validateUtils.isAvailablePhoneNumberFormat(sellerInfo.getBusinessContactTelNum());
        validateUtils.isAvailableEmailFormat(sellerInfo.getBusinessContactEmail());

        log.info("[RegisterSeller] Validate seller info for seller registration, OK!!");
    }

    protected String validateAndEncryptPassword(String password) {
        validateUtils.isAvailablePasswordFormat(password);
        return authUtils.encryptPassword(password);
    }

    public JWTUserInfo checkLoginCredentials(String loginId, String password) {
        Seller seller = sellerRepository.findByLoginId(loginId).orElseThrow(()->
                new UserLoginException(ResultCode.MEMBER_NOT_FOUND));
        log.info("[CheckLoginCredential] Member loginId : {}", seller.getLoginId());
        authUtils.checkPassword(seller.getPassword(), password);
        JWTUserInfo userInfo = new JWTUserInfo();
        userInfo.of(seller);
        return userInfo;
    }
}

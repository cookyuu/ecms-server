package com.cookyuu.ecms_server.domain.seller.dto;

import com.cookyuu.ecms_server.global.code.ResultCode;
import com.cookyuu.ecms_server.global.exception.domain.ECMSProductException;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

public class UpdateSellerDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        @NotNull
        private String password;
        private String name;
        private String businessName;
        private String businessAddress;
        private String businessContactTelNum;
        private String businessContactEmail;

        public void chkAllNull() {
            if (StringUtils.isEmpty(this.name) && StringUtils.isEmpty(this.businessName) && StringUtils.isEmpty(this.businessAddress)
                    && StringUtils.isEmpty(this.businessContactTelNum) && StringUtils.isEmpty(this.businessContactEmail)) {
                throw new ECMSProductException(ResultCode.REQUEST_DATA_ISNULL);
            }
        }
    }
}

package com.cookyuu.ecms_server.domain.product.dto;

import com.cookyuu.ecms_server.global.dto.ResultCode;
import com.cookyuu.ecms_server.global.exception.domain.ECMSProductException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

public class UpdateProductDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String name;
        private String description;
        private Integer price;
        private Integer stockQuantity;
        private String categoryName;

        public void chkAllNull() {
            if (StringUtils.isEmpty(this.name) && StringUtils.isEmpty(this.description) && StringUtils.isEmpty(this.price)
                    && StringUtils.isEmpty(this.stockQuantity) && StringUtils.isEmpty(this.categoryName)) {
                throw new ECMSProductException(ResultCode.REQUEST_DATA_ISNULL);
            }
        }
    }
}

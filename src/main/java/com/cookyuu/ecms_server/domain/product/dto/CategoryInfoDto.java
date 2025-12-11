package com.cookyuu.ecms_server.domain.product.dto;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CategoryInfoDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String name;
        private String parentCategoryName;

        public void chkAllNull() {
            if (StringUtils.isEmpty(name) || StringUtils.isEmpty(parentCategoryName)) {
                throw new BusinessException(ResultCode.REQUEST_DATA_ISNULL);
            }
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Response {
        private Long categoryId;
    }

}

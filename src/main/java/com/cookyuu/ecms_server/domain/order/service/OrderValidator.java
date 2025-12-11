package com.cookyuu.ecms_server.domain.order.service;

import com.cookyuu.ecms_server.common.enums.ResultCode;
import com.cookyuu.ecms_server.common.exception.BusinessException;
import com.cookyuu.ecms_server.domain.order.logging.OrderLogHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.cookyuu.ecms_server.common.enums.ResultCode.ORDER_PROCESS_FAIL;

@Component
@RequiredArgsConstructor
public class OrderValidator {

    private final OrderLogHelper orderLogHelper;

    public void validateStockQuantity(int quantity, Integer stockQuantity, Long productId) {
        if (stockQuantity == 0) {
            orderLogHelper.logStockError(quantity, stockQuantity, ResultCode.PRODUCT_SOLD_OUT,
                "Product sold out - zero stock");
            throw new BusinessException(ResultCode.PRODUCT_SOLD_OUT, "주문하신 상품의 재고 수량이 없습니다.");
        }
        if (quantity > stockQuantity) {
            orderLogHelper.logStockError(quantity, stockQuantity, ResultCode.PRODUCT_SOLD_OUT,
                "Insufficient stock - order quantity exceeds available stock");
            throw new BusinessException(ResultCode.PRODUCT_SOLD_OUT,
                "주문하신 상품의 재고 수량이 부족합니다. 재고 수량 : " + stockQuantity);
        }
        orderLogHelper.logValidationSuccess("validateStockQuantity", productId, quantity, stockQuantity);
    }

    public void validateProductPrice(int orderPrice, Integer currentPrice, Long productId) {
        if (currentPrice == null) {
            orderLogHelper.logPriceError(productId, orderPrice, null, ORDER_PROCESS_FAIL,
                "Product price validation failed - price not set");
            throw new BusinessException(ORDER_PROCESS_FAIL, "가격이 아직 책정되지 않은 상품이 있습니다.");
        }
        if (orderPrice != currentPrice) {
            orderLogHelper.logPriceError(productId, orderPrice, currentPrice, ORDER_PROCESS_FAIL,
                "Price validation failed - order price and current price mismatch");
            throw new BusinessException(ORDER_PROCESS_FAIL, "상품의 현재 가격과 주문 가격이 일치하지 않습니다.");
        }
        orderLogHelper.logValidationSuccess("validateProductPrice", productId, null, null);
    }
}

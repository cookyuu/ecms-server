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

    /**
     * 주문 수량과 재고 수량을 비교하여 검증
     *
     * @param quantity 주문 수량
     * @param stockQuantity 재고 수량
     * @param productId 상품 ID (로깅용)
     * @throws BusinessException 재고가 없거나 부족한 경우
     */
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

    /**
     * 주문 가격과 현재 상품 가격을 비교하여 검증
     *
     * @param orderPrice 주문 시점의 가격
     * @param currentPrice 현재 상품 가격
     * @param productId 상품 ID
     * @throws BusinessException 가격이 설정되지 않았거나 일치하지 않는 경우
     */
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

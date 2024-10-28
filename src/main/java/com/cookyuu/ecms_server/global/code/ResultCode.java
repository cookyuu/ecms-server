package com.cookyuu.ecms_server.global.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResultCode {

    // Common - SUCCESS
    SUCCESS(HttpStatus.OK,"0000","성공!"),
    CREATED(HttpStatus.CREATED,"0000", "성공!"),

    // Common - ERROR
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST,"C-001", "잘못된 요청값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C-002", "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"C-004", "서버 에러 입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST,"C-005", "잘못된 유형의 값입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C-006", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND,"C-007", "데이터를 찾을 수 없습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "C-008", "잘못된 요청입니다."),
    BAD_CREDENTIAL(HttpStatus.UNAUTHORIZED, "C-009", "자격 증명에 실패했습니다."),
    REQUEST_DATA_ISNULL(HttpStatus.BAD_REQUEST,"C-010" , "요청 데이터가 모두 NULL 입니다."),
    REDIS_COMMON_EXP(HttpStatus.INTERNAL_SERVER_ERROR,"U-001","Redis Exception 발생. "),

    // AUTH - SUCCESS
    NORMAL_LOGIN_SUCCESS(HttpStatus.OK, "0000", "일반 로그인 성공."),
    OAUTH_LOGIN_SUCCESS(HttpStatus.OK, "0000", "SNS 로그인 성공."),
    SELLER_LOGIN_SUCCESS(HttpStatus.OK, "0000", "판매자 로그인 성공."),
    LOGOUT_SUCCESS(HttpStatus.OK, "0000", "로그아웃 성공."),
    SIGNUP_SUCCESS(HttpStatus.CREATED,"0000" ,"회원가입 성공" ),

    // AUTH - ERROR
    ALREADY_LOGOUT_USER(HttpStatus.BAD_REQUEST,"A-001", "이미 로그아웃된 유저입니다. 다시 로그인해주세요."),
    CONFIRM_PASSWORD_UNMATCHED(HttpStatus.BAD_REQUEST, "A-002", "확인용 패스워드가 일치하지 않습니다. 동일한 패스워드를 입력해주세요." ),

    // JWT - ERROR
    JWT_SECURE_EXP(HttpStatus.FORBIDDEN, "J-001", "토큰 보안 오류 발생. "),
    JWT_INVALID_TOKEN(HttpStatus.BAD_REQUEST, "J-002", "유효하지 않은 토큰입니다."),
    JWT_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "J-003", "토큰 유효기한이 만료되었습니다."),
    JWT_UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST, "J-004", "지원하지 않는 토큰입니다."),
    JWT_EMPTY_TOKEN(HttpStatus.BAD_REQUEST, "J-005", "토큰 데이터가 비어있습니다."),
    JWT_ALREADY_LOGOUT(HttpStatus.UNAUTHORIZED, "J-006", "이미 로그아웃된 사용자입니다."),

    // Member - SUCCESS
    VALID_USERID_SUCCESS(HttpStatus.OK, "0000", "유저 아이디 유효성 검증 완료. 사용 가능한 유저 아이디입니다."),

    // Member - ERROR
    VALID_EMAIL_DUPLICATE(HttpStatus.BAD_REQUEST, "M-001", "이메일 유효성 검증 실패. 이미 등록된 이메일입니다."),
    VALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "M-002", "이메일 유효성 검증 실패. 잘못된 이메일 형식입니다."),
    VALID_PHONENUMBER_DUPLICATE(HttpStatus.BAD_REQUEST,"M-003", "핸드폰 번호 유효성 검증 실패. 이미 등록된 핸드폰 번호입니다."),
    VALID_PHONENUMBER_FORMAT(HttpStatus.BAD_REQUEST, "M-004", "핸드폰 번호 유효성 검증 실패. 잘못된 형식의 핸드폰 번호입니다."),
    VALID_LOGINID_DUPLICATE(HttpStatus.BAD_REQUEST,"M-005", "로그인 아이디 유효성 검증 실패. 이미 등록된 아이디입니다."),
    VALID_LOGINID_FORMAT(HttpStatus.BAD_REQUEST, "M-006", "로그인 아이디 유효성 검증 실패. 잘못된 아이디 형식입니다."),
    VALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "M-007", "패스워드 유효성 검증 실패. 잘못된 패스워드 형식입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M-008", "해당 유저를 찾을 수 없습니다."),

    // Category - ERROR
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CA-001", "해당 카테고리를 찾을 수 없습니다."),
    CATEGORY_NAME_DUPLICATED(HttpStatus.BAD_REQUEST, "CA-002", "이미 등록된 카테고리명입니다."),

    // Seller - ERROR
    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "S-001", "해당 판매자를 찾을 수 없습니다."),
    VALID_BUSINESSNUM_FORMAT(HttpStatus.BAD_REQUEST,"S-002" ,"사업자등록번호 유효성 검증 실패. 잘못된 형식의 사업자등록번호입니다." ),
    VALID_BUSINESSNUM_DUPLICATE(HttpStatus.BAD_REQUEST,"S-003" ,"사업자등록번호 유효성 검증 실패. 이미 등록된 사업자 등록번호입니다." ),

    // Product - ERROR
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P-001", "해당 상품을 찾을 수 없습니다."),
    PRODUCT_OWNER_UNMATCHED(HttpStatus.BAD_REQUEST,"P-002", "상품에 대한 권한이 없습니다. 해당 상품의 판매자가 아닙니다." ),
    ALREADY_DELETED_PRODUCT(HttpStatus.BAD_REQUEST, "P-003" ,"이미 삭제된 상품입니다."),
    PRODUCT_SOLD_OUT(HttpStatus.BAD_REQUEST,"P-002" , "상품 재고가 없습니다."),

    // Cart - ERROR
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CT-001", "유저의 카트정보를 찾을 수 없습니다."),
    CARTITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CT-002","카트에 등록된 해당 상품을 찾을 없습니다."),

    // Order - SUCCESS
    ORDER_CANCEL_SUCCESS(HttpStatus.OK, "0000", "주문 취소가 정상 처리되었습니다."),
    ORDER_REVISE_SUCCESS(HttpStatus.OK, "0000", "주문 정보 변경이 완료되었습니다."),

    // Order - ERROR
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O-001", "주문 정보를 찾을 수 없습니다."),
    ORDER_PROCESS_FAIL(HttpStatus.BAD_REQUEST,"O-002" , "주문 처리에 실패했습니다."),
    ORDER_CANCEL_FAIL(HttpStatus.BAD_REQUEST, "O-003", "주문 취소에 실패했습니다."),
    ORDER_STATUS_ERROR(HttpStatus.BAD_REQUEST, "O-004", "해당 작업을 할 수 있는 주문 상태가 아닙니다."),
    ALREADY_CANCELED_ORDER(HttpStatus.BAD_REQUEST,"O-005","이미 취소된 주문입니다."),
    ORDER_SELLER_UNMATCHED(HttpStatus.FORBIDDEN, "O-006", "해당 주문에 해당하는 상품의 판매자가 아닙니다."),
    ORDER_BUYER_UNMATCHED(HttpStatus.FORBIDDEN, "O-007", "해당 주문의 구매자가 아닙니다."),

    // Payment - ERROR
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P-001","결제 정보를 찾을 수 없습니다."),
    PAYMENT_PRICE_UNMATCHED(HttpStatus.BAD_REQUEST, "P-002", "결제 금액이 주문 금액과 일치하지 않습니다."),
    PAYMENT_BUYER_UNMATCHED(HttpStatus.FORBIDDEN, "P-003", "해당 주문의 주문자와 결제자가 일치하지 않습니다."),
    PAYMENT_IMPOSSIBLE_STATUS(HttpStatus.BAD_REQUEST,"P-004", "결제 할 수 없는 주문 상태입니다."),
    PAYMENT_IMPOSSIBLE_STATUS_CANCEL(HttpStatus.BAD_REQUEST,"P-005", "결제 취소를 할 수 없는 주문 상태입니다."),
    PAYMENT_INACCESSIBLE_DETAIL(HttpStatus.FORBIDDEN, "P-006", "요청 유저의 결제 정보가 아닙니다."),

    // Shipment - ERROR
    SHIPMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "SP-001", "배송 정보를 찾을 수 없습니다."),
    SHIPMENT_STATUS_UNMATCHED(HttpStatus.BAD_REQUEST, "SP-002", "요청한 작업을 할 수 있는 배송 상태가 아닙니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ResultCode (HttpStatus status, final String code, final String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

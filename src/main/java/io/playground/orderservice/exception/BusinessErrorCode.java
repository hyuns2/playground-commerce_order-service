package io.playground.orderservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BusinessErrorCode {
    // 400
    ORDER_PROCESS_FAILED("ORDER-400:001", "주문 처리에 실패했습니다.", HttpStatus.BAD_REQUEST),
    ORDER_PAYMENT_FAILED("ORDER-400:002", "결제 처리에 실패했습니다.", HttpStatus.BAD_REQUEST),
    ORDER_EXPIRED_FAILED("ORDER-400:003", "주문 만료 처리에 실패했습니다.", HttpStatus.BAD_REQUEST),
    ORDER_CANCELLATION_FAILED("ORDER-400:004", "주문 취소에 실패했습니다.", HttpStatus.BAD_REQUEST),
    ORDER_PARTIAL_CANCELLATION_FAILED("ORDER-400:005", "부분 취소에 실패했습니다.", HttpStatus.BAD_REQUEST),

    // 404
    ORDER_NOT_FOUND("ORDER-404:001", "해당하는 주문을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 500
    JSON_PROCESSING_FAILED("ORDER-500:001", "JSON 직렬화 처리에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    CATALOG_SERVICE_FAILED("ORDER-500:002", "카탈로그 서비스 호출에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVENTORY_SERVICE_FAILED("ORDER-500:003", "인벤토리 서비스 호출에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_SERVICE_FAILED("ORDER-500:004", "결제 서비스 호출에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}

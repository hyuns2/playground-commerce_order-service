package io.playground.orderservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessErrorDto {
    private final String code;
    private final String message;
    private final String detail;
    private final HttpStatus httpStatus;

    public BusinessErrorDto(BusinessErrorCode errorCode,
                            String detail) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.detail = detail;
        this.httpStatus = errorCode.getHttpStatus();
    }

    public static BusinessErrorDto from(BusinessDetailException e) {
        return new BusinessErrorDto(
                e.getErrorCode(),
                e.getDetail()
        );
    }
}

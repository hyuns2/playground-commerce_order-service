package io.playground.orderservice.exception;

public class BusinessException extends RuntimeException {
    private final BusinessErrorCode errorCode;

    public BusinessException(BusinessErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public BusinessErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return getErrorCode().name() + ": " + getErrorCode().getMessage();
    }
}

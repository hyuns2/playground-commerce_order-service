package io.playground.orderservice.exception;

public class BusinessDetailException extends RuntimeException {
    private final BusinessErrorCode errorCode;
    private final String detail;

    public BusinessDetailException(BusinessErrorCode errorCode,
                                   String detail) {
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public BusinessErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return getErrorCode().name() + ": " + getErrorCode().getMessage();
    }

    public String getDetail() {
        return detail;
    }
}

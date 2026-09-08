package io.playground.orderservice.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class BusinessExceptionHandler {
    @ExceptionHandler(BusinessException::class)
    protected fun handleException(e: BusinessException): ResponseEntity<BusinessErrorDto> {
        val errorCode = e.errorCode
        return ResponseEntity
            .status(errorCode.httpStatus.value())
            .body(BusinessErrorDto(errorCode, null))
    }

    @ExceptionHandler(BusinessDetailException::class)
    protected fun handleException(e: BusinessDetailException): ResponseEntity<BusinessErrorDto> {
        val errorCode = e.errorCode
        return ResponseEntity
            .status(errorCode.httpStatus.value())
            .body(BusinessErrorDto(errorCode, e.detail))
    }
}

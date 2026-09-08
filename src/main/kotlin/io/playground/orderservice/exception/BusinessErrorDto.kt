package io.playground.orderservice.exception

import org.springframework.http.HttpStatus

class BusinessErrorDto(
    val code: String,
    val message: String,
    val detail: String?,
    val httpStatus: HttpStatus,
) {
    constructor(errorCode: BusinessErrorCode, detail: String?) : this(
        errorCode.code,
        errorCode.message,
        detail,
        errorCode.httpStatus,
    )

    companion object {
        @JvmStatic
        fun from(e: BusinessDetailException): BusinessErrorDto =
            BusinessErrorDto(e.errorCode, e.detail)
    }
}

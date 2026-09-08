package io.playground.orderservice.exception

class BusinessDetailException(
    val errorCode: BusinessErrorCode,
    val detail: String,
) : RuntimeException() {
    override val message: String
        get() = "${errorCode.name}: ${errorCode.message}"
}

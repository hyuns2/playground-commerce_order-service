package io.playground.orderservice.exception

class BusinessException(
    val errorCode: BusinessErrorCode,
) : RuntimeException() {
    override val message: String
        get() = "${errorCode.name}: ${errorCode.message}"
}

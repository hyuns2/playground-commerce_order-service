package io.playground.orderservice.application.order.dto

import kotlin.jvm.JvmRecord

class OrderDto {
    @JvmRecord
    data class DoOrderRequest(
        val orderExternalId: String,
        val variantQuantities: Map<Long, Int>,
    )

    @JvmRecord
    data class DoPaymentRequest(
        val idempotencyKey: String,
        val orderExternalId: String,
        val paymentKey: String,
        val amount: java.math.BigDecimal,
    )

    @JvmRecord
    data class CancelAllRequest(
        val idempotencyKey: String,
        val orderExternalId: String,
        val paymentKey: String,
        val reason: String,
    )

    @JvmRecord
    data class CancelPartiallyRequest(
        val idempotencyKey: String,
        val orderExternalId: String,
        val paymentKey: String,
        val variantQuantities: Map<Long, Int>,
        val reason: String,
    )
}

package io.playground.orderservice.application.order.dto

import io.playground.orderservice.domain.order.Order
import java.math.BigDecimal
import kotlin.jvm.JvmRecord

@JvmRecord
data class OrderAmountInfo(
    val status: Order.OrderStatus,
    val orderId: Long,
    val finalAmount: BigDecimal,
)

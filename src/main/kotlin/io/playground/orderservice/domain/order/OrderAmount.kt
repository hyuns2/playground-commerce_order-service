package io.playground.orderservice.domain.order

import java.math.BigDecimal

class OrderAmount(
    val id: Long?,
    val orderId: Long?,
    val totalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val deliveryFee: BigDecimal,
    val finalAmount: BigDecimal,
) {
    companion object {
        @JvmStatic
        fun of(
            id: Long?,
            orderId: Long?,
            totalAmount: BigDecimal,
            discountAmount: BigDecimal,
            deliveryFee: BigDecimal,
            finalAmount: BigDecimal,
        ): OrderAmount = OrderAmount(id, orderId, totalAmount, discountAmount, deliveryFee, finalAmount)
    }
}

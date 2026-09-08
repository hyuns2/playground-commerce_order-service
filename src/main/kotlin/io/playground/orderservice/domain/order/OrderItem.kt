package io.playground.orderservice.domain.order

import java.math.BigDecimal

class OrderItem(
    val id: Long?,
    val orderId: Long?,
    val variantId: Long,
    val variantName: String,
    val productId: Long,
    val productName: String,
    val price: BigDecimal,
    val quantity: Int,
    val status: OrderItemStatus,
    val canceledQuantity: Int,
    val canceledReason: String?,
) {
    enum class OrderItemStatus {
        NONE,
        PREPARING,
        DELIVERED,
        COMPLETED,
    }

    companion object {
        @JvmStatic
        fun of(
            id: Long?,
            orderId: Long?,
            variantId: Long,
            variantName: String,
            productId: Long,
            productName: String,
            price: BigDecimal,
            quantity: Int,
            status: OrderItemStatus,
            canceledQuantity: Int,
            canceledReason: String?,
        ): OrderItem = OrderItem(
            id,
            orderId,
            variantId,
            variantName,
            productId,
            productName,
            price,
            quantity,
            status,
            canceledQuantity,
            canceledReason,
        )
    }
}

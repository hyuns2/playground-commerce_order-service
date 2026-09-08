package io.playground.orderservice.application.order.port

import io.playground.orderservice.domain.order.Order
import io.playground.orderservice.domain.order.OrderItem

interface OrderItemPersistencePort {
    fun findAllByOrderExternalIdAndOrderStatus(
        orderExternalId: String,
        orderStatus: Order.OrderStatus,
    ): List<OrderItem>

    fun findAllByVariantIdsAndOrderExternalIdAndOrderStatusIn(
        orderExternalId: String,
        variantIds: List<Long>,
        orderStatuses: List<Order.OrderStatus>,
    ): List<OrderItem>

    fun saveAll(orderItems: List<OrderItem>)

    fun updateCanceledReasonsToCancelAll(
        orderExternalId: String,
        reason: String,
    ): Boolean

    fun updateCanceledInfosToCancelPartially(
        orderExternalId: String,
        cancelsItemQuantities: Map<Long, Int>,
        reason: String,
    ): Boolean
}

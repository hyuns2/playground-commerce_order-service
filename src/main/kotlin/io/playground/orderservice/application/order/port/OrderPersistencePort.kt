package io.playground.orderservice.application.order.port

import io.playground.orderservice.domain.order.Order
import java.util.Optional

@JvmSuppressWildcards
interface OrderPersistencePort {
    fun findByExternalId(externalId: String): Optional<Order>

    fun save(order: Order): Order

    fun updateStatusByExternalId(
        externalId: String,
        status: Order.OrderStatus,
        beforeStatuses: List<Order.OrderStatus>,
    ): Boolean
}

package io.playground.orderservice.infrastructure.persistence.order.adapter

import io.playground.orderservice.application.order.port.OrderPersistencePort
import io.playground.orderservice.domain.order.Order
import io.playground.orderservice.infrastructure.persistence.order.entity.OrderEntity
import io.playground.orderservice.infrastructure.persistence.order.repository.OrderJpaRepository
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class OrderPersistenceAdapter(
    private val orderRepository: OrderJpaRepository,
) : OrderPersistencePort {
    override fun findByExternalId(externalId: String): Optional<Order> =
        orderRepository.findByExternalId(externalId)
            .map(OrderEntity::toDomain)

    override fun save(order: Order): Order =
        orderRepository.save(OrderEntity.fromDomain(order)).toDomain()

    override fun updateStatusByExternalId(
        externalId: String,
        status: Order.OrderStatus,
        beforeStatuses: List<Order.OrderStatus>,
    ): Boolean = if (beforeStatuses.isEmpty()) {
        orderRepository.updateStatusByExternalId(externalId, status) > 0
    } else {
        orderRepository.updateStatusByExternalId(externalId, status, beforeStatuses) > 0
    }
}

package io.playground.orderservice.infrastructure.persistence.order.adapter

import io.playground.orderservice.application.order.port.OrderItemPersistencePort
import io.playground.orderservice.domain.order.Order
import io.playground.orderservice.domain.order.OrderItem
import io.playground.orderservice.infrastructure.persistence.order.entity.OrderItemEntity
import io.playground.orderservice.infrastructure.persistence.order.repository.OrderItemJdbcTemplate
import io.playground.orderservice.infrastructure.persistence.order.repository.OrderItemJpaRepository
import io.playground.orderservice.infrastructure.persistence.order.repository.OrderJpaRepository
import org.springframework.stereotype.Component

@Component
class OrderItemPersistenceAdapter(
    private val orderItemRepository: OrderItemJpaRepository,
    private val orderItemTemplate: OrderItemJdbcTemplate,
    private val orderRepository: OrderJpaRepository,
) : OrderItemPersistencePort {
    override fun findAllByOrderExternalIdAndOrderStatus(
        orderExternalId: String,
        orderStatus: Order.OrderStatus,
    ): List<OrderItem> = orderItemRepository
        .findAllByOrder_ExternalIdAndOrder_Status(
            orderExternalId,
            Order.OrderStatus.PAID,
        ).map(OrderItemEntity::toDomain)

    override fun findAllByVariantIdsAndOrderExternalIdAndOrderStatusIn(
        orderExternalId: String,
        variantIds: List<Long>,
        orderStatuses: List<Order.OrderStatus>,
    ): List<OrderItem> = orderItemRepository
        .findAllByVariantIdInAndOrder_ExternalIdAndOrder_StatusIn(
            variantIds,
            orderExternalId,
            orderStatuses,
        ).map(OrderItemEntity::toDomain)

    override fun saveAll(orderItems: List<OrderItem>) {
        val orderReference = orderRepository.getReferenceById(
            requireNotNull(orderItems.first().orderId) { "orderItems[0].orderId is required" },
        )
        orderItemRepository.saveAll(
            orderItems.map { entity ->
                OrderItemEntity.fromDomain(entity, orderReference)
            },
        )
    }

    override fun updateCanceledReasonsToCancelAll(orderExternalId: String, reason: String): Boolean =
        orderItemRepository.updateCanceledReasonsToCancelAll(orderExternalId, reason) > 0

    override fun updateCanceledInfosToCancelPartially(
        orderExternalId: String,
        cancelsItemQuantities: Map<Long, Int>,
        reason: String,
    ): Boolean = orderItemTemplate
        .updateCanceledInfosToCancelPartially(orderExternalId, cancelsItemQuantities, reason)
        .all { it > 0 }
}

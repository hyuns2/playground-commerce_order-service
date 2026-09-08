package io.playground.orderservice.infrastructure.persistence.order.repository

import io.playground.orderservice.domain.order.Order
import io.playground.orderservice.infrastructure.persistence.order.entity.OrderItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface OrderItemJpaRepository : JpaRepository<OrderItemEntity, Long> {
    fun findAllByOrder_ExternalIdAndOrder_Status(
        orderExternalId: String,
        status: Order.OrderStatus,
    ): List<OrderItemEntity>

    fun findAllByVariantIdInAndOrder_ExternalIdAndOrder_StatusIn(
        variantIds: List<Long>,
        orderExternalId: String,
        statuses: List<Order.OrderStatus>,
    ): List<OrderItemEntity>

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        "update OrderItemEntity oi set oi.canceledQuantity = oi.quantity, oi.canceledReason = :reason " +
            "where oi.order.externalId = :orderExternalId and oi.canceledQuantity = 0",
    )
    fun updateCanceledReasonsToCancelAll(orderExternalId: String, reason: String): Int
}

package io.playground.orderservice.infrastructure.persistence.order.repository

import io.playground.orderservice.domain.order.Order
import io.playground.orderservice.infrastructure.persistence.order.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface OrderJpaRepository : JpaRepository<OrderEntity, Long> {
    fun findByExternalId(externalId: String): Optional<OrderEntity>

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        "update OrderEntity o set o.status = :status where o.externalId = :externalId",
    )
    fun updateStatusByExternalId(externalId: String, status: Order.OrderStatus): Int

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        "update OrderEntity o set o.status = :status where o.externalId = :externalId and o.status in :beforeStatuses",
    )
    fun updateStatusByExternalId(
        externalId: String,
        status: Order.OrderStatus,
        beforeStatuses: List<Order.OrderStatus>,
    ): Int
}

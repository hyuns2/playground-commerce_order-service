package io.playground.orderservice.infrastructure.persistence.order.repository

import io.playground.orderservice.application.order.dto.OrderAmountInfo
import io.playground.orderservice.infrastructure.persistence.order.entity.OrderAmountEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface OrderAmountJpaRepository : JpaRepository<OrderAmountEntity, Long> {
    @Query(
        "SELECT new io.playground.orderservice.application.order.dto.OrderAmountInfo(o.status, o.id, oa.finalAmount) " +
            "FROM OrderAmountEntity oa JOIN oa.order o WHERE o.externalId = :orderExternalId",
    )
    fun findOrderAmountInfoByOrderExternalId(orderExternalId: String): Optional<OrderAmountInfo>
}

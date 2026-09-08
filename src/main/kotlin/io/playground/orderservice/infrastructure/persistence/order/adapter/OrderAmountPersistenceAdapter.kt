package io.playground.orderservice.infrastructure.persistence.order.adapter

import io.playground.orderservice.application.order.dto.OrderAmountInfo
import io.playground.orderservice.application.order.port.OrderAmountPersistencePort
import io.playground.orderservice.domain.order.OrderAmount
import io.playground.orderservice.infrastructure.persistence.order.entity.OrderAmountEntity
import io.playground.orderservice.infrastructure.persistence.order.repository.OrderAmountJpaRepository
import io.playground.orderservice.infrastructure.persistence.order.repository.OrderJpaRepository
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class OrderAmountPersistenceAdapter(
    private val orderAmountRepository: OrderAmountJpaRepository,
    private val orderRepository: OrderJpaRepository,
) : OrderAmountPersistencePort {
    override fun findOrderAmountInfoByOrderExternalId(orderExternalId: String): Optional<OrderAmountInfo> =
        orderAmountRepository.findOrderAmountInfoByOrderExternalId(orderExternalId)

    override fun save(orderAmount: OrderAmount): OrderAmount =
        orderAmountRepository.save(
            OrderAmountEntity.fromDomain(
                orderAmount,
                orderRepository.getReferenceById(requireNotNull(orderAmount.orderId) { "orderAmount.orderId is required" }),
            ),
        ).toDomain()
}

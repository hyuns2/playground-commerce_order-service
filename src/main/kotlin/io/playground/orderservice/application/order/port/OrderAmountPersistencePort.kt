package io.playground.orderservice.application.order.port

import io.playground.orderservice.application.order.dto.OrderAmountInfo
import io.playground.orderservice.domain.order.OrderAmount
import java.util.Optional

interface OrderAmountPersistencePort {
    fun findOrderAmountInfoByOrderExternalId(orderExternalId: String): Optional<OrderAmountInfo>

    fun save(orderAmount: OrderAmount): OrderAmount
}

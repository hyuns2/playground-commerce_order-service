package io.playground.orderservice.application.order.usecase

import io.playground.orderservice.application.order.port.OrderAmountPersistencePort
import io.playground.orderservice.application.order.port.OrderItemPersistencePort
import io.playground.orderservice.application.order.port.OrderPersistencePort
import io.playground.orderservice.application.saga.dto.ClientDto
import io.playground.orderservice.domain.order.Order
import io.playground.orderservice.domain.order.OrderAmount
import io.playground.orderservice.domain.order.OrderItem
import io.playground.orderservice.exception.BusinessDetailException
import io.playground.orderservice.exception.BusinessErrorCode
import io.playground.orderservice.exception.BusinessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class OrderService(
    private val orderPersistence: OrderPersistencePort,
    private val orderAmountPersistence: OrderAmountPersistencePort,
    private val orderItemPersistence: OrderItemPersistencePort,
) {
    @Transactional(readOnly = true)
    fun getOrder(orderExternalId: String): Order =
        orderPersistence.findByExternalId(orderExternalId)
            .orElseThrow { BusinessException(BusinessErrorCode.ORDER_NOT_FOUND) }

    @Transactional
    fun createOrder(
        userId: Long,
        orderExternalId: String,
        variantQuantities: Map<Long, Int>,
        snapshots: List<ClientDto.Snapshot>,
    ): Order {
        val existingOrder = getExistingOrder(orderExternalId)
        if (existingOrder != null) {
            return existingOrder
        }

        val order = orderPersistence.save(
            Order.of(null, orderExternalId, userId, Order.OrderStatus.CREATED),
        )

        val orderItems = snapshots.map { info ->
            OrderItem.of(
                null,
                order.id,
                info.variantId(),
                info.variantName(),
                info.productId(),
                info.productName(),
                info.price(),
                variantQuantities[info.variantId()] ?: 0,
                OrderItem.OrderItemStatus.NONE,
                0,
                null,
            )
        }
        orderItemPersistence.saveAll(orderItems)

        val totalAmount = orderItems.map { item ->
            item.price.multiply(BigDecimal.valueOf(item.quantity.toLong()))
        }.reduce(BigDecimal.ZERO, BigDecimal::add)

        orderAmountPersistence.save(
            OrderAmount.of(
                null,
                order.id,
                totalAmount,
                BigDecimal.ZERO,
                DELIVERY_FEE,
                totalAmount.add(DELIVERY_FEE),
            ),
        )

        return order
    }

    @Transactional(readOnly = true)
    fun getExistingOrder(orderExternalId: String): Order? =
        orderPersistence.findByExternalId(orderExternalId).orElse(null)

    @Transactional(readOnly = true)
    fun validatePayment(orderExternalId: String, amount: BigDecimal) {
        val orderAmountInfo = orderAmountPersistence
            .findOrderAmountInfoByOrderExternalId(orderExternalId)
            .orElseThrow {
                BusinessDetailException(
                    BusinessErrorCode.ORDER_PAYMENT_FAILED,
                    "ORDER_NOT_FOUND",
                )
            }

        if (orderAmountInfo.status() != Order.OrderStatus.CREATED) {
            throw BusinessDetailException(
                BusinessErrorCode.ORDER_PAYMENT_FAILED,
                "ORDER_STATUS: ${orderAmountInfo.status()}",
            )
        }

        if (orderAmountInfo.finalAmount().compareTo(amount) != 0) {
            throw BusinessDetailException(
                BusinessErrorCode.ORDER_PAYMENT_FAILED,
                "ORDER_AMOUNT: ${orderAmountInfo.finalAmount()}",
            )
        }
    }

    @Transactional
    fun completeOrder(orderExternalId: String): Boolean =
        orderPersistence.updateStatusByExternalId(
            orderExternalId,
            Order.OrderStatus.PAID,
            listOf(Order.OrderStatus.CREATED),
        )

    @Transactional
    fun failOrder(orderExternalId: String): Boolean =
        orderPersistence.updateStatusByExternalId(
            orderExternalId,
            Order.OrderStatus.FAILED,
            listOf(
                Order.OrderStatus.CREATED,
                Order.OrderStatus.PAID,
            ),
        )

    companion object {
        private val DELIVERY_FEE: BigDecimal = BigDecimal.valueOf(3000)
    }
}

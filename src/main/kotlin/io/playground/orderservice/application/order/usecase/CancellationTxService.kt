package io.playground.orderservice.application.order.usecase

import io.playground.orderservice.application.eventstream.EventProducerPort
import io.playground.orderservice.application.eventstream.OrderEvent
import io.playground.orderservice.application.order.port.OrderItemPersistencePort
import io.playground.orderservice.application.order.port.OrderPersistencePort
import io.playground.orderservice.domain.order.Order
import io.playground.orderservice.exception.BusinessDetailException
import io.playground.orderservice.exception.BusinessErrorCode
import io.playground.orderservice.exception.BusinessException
import io.playground.orderservice.infrastructure.util.JsonUtil
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
class CancellationTxService(
    private val orderPersistence: OrderPersistencePort,
    private val orderItemPersistence: OrderItemPersistencePort,
    private val eventProducer: EventProducerPort,
    private val jsonUtil: JsonUtil,
) {
    @Transactional(readOnly = true)
    fun validateToCancelAll(orderExternalId: String) {
        val orderItems = orderItemPersistence.findAllByOrderExternalIdAndOrderStatus(
            orderExternalId,
            Order.OrderStatus.PAID,
        )

        if (orderItems.isEmpty() || !orderItems.all { it.canceledQuantity == 0 }) {
            throw BusinessDetailException(
                BusinessErrorCode.ORDER_CANCELLATION_FAILED,
                "ORDER_NOT_FOUND OR INVALID_ORDER_STATUS",
            )
        }
    }

    @Transactional
    fun cancelAll(
        idempotencyKey: String,
        orderExternalId: String,
        reason: String,
    ) {
        if (!orderPersistence.updateStatusByExternalId(
                orderExternalId,
                Order.OrderStatus.CANCELED,
                listOf(Order.OrderStatus.PAID),
            )
        ) {
            throw BusinessException(BusinessErrorCode.ORDER_CANCELLATION_FAILED)
        }

        if (!orderItemPersistence.updateCanceledReasonsToCancelAll(orderExternalId, reason)) {
            throw BusinessException(BusinessErrorCode.ORDER_CANCELLATION_FAILED)
        }

        eventProducer.produce(
            OrderEvent.EventType.ALL_CANCELED,
            OrderEvent.AllCanceled.builder()
                .orderExternalId(orderExternalId)
                .build(),
            idempotencyKey,
        )
    }

    @Transactional(readOnly = true)
    fun validateAndCalculateAmountToCancelPartially(
        orderExternalId: String,
        variantQuantities: Map<Long, Int>,
    ): BigDecimal {
        val unavailable = HashMap<Long, Int>()
        val orderItems = orderItemPersistence
            .findAllByVariantIdsAndOrderExternalIdAndOrderStatusIn(
                orderExternalId,
                variantQuantities.keys.toList(),
                listOf(
                    Order.OrderStatus.PAID,
                    Order.OrderStatus.PARTIAL_CANCELED,
                ),
            )
            .onEach { orderItem ->
                if (orderItem.quantity < orderItem.canceledQuantity + (variantQuantities[orderItem.variantId] ?: 0)) {
                    unavailable[orderItem.variantId] = orderItem.quantity - orderItem.canceledQuantity
                }
            }

        if (orderItems.isEmpty()) {
            throw BusinessDetailException(
                BusinessErrorCode.ORDER_PARTIAL_CANCELLATION_FAILED,
                "ORDER_NOT_FOUND OR INVALID_ORDER_STATUS",
            )
        }
        if (unavailable.isNotEmpty()) {
            throw BusinessDetailException(
                BusinessErrorCode.ORDER_PARTIAL_CANCELLATION_FAILED,
                jsonUtil.toJson(unavailable),
            )
        }

        return orderItems.map { orderItem ->
            orderItem.price.multiply(
                BigDecimal.valueOf((variantQuantities[orderItem.variantId] ?: 0).toLong()),
            )
        }.reduce(BigDecimal.ZERO, BigDecimal::add)
    }

    @Transactional
    fun cancelPartially(
        idempotencyKey: String,
        orderExternalId: String,
        variantQuantities: Map<Long, Int>,
        reason: String,
        canceledAmount: BigDecimal,
    ) {
        if (!orderItemPersistence.updateCanceledInfosToCancelPartially(orderExternalId, variantQuantities, reason)) {
            throw BusinessException(BusinessErrorCode.ORDER_PARTIAL_CANCELLATION_FAILED)
        }

        if (!orderPersistence.updateStatusByExternalId(
                orderExternalId,
                Order.OrderStatus.PARTIAL_CANCELED,
                listOf(
                    Order.OrderStatus.PAID,
                    Order.OrderStatus.PARTIAL_CANCELED,
                ),
            )
        ) {
            throw BusinessException(BusinessErrorCode.ORDER_PARTIAL_CANCELLATION_FAILED)
        }

        eventProducer.produce(
            OrderEvent.EventType.PARTIALLY_CANCELED,
            OrderEvent.PartiallyCanceled.builder()
                .idempotencyKey(idempotencyKey)
                .orderExternalId(orderExternalId)
                .canceledVariantQuantities(variantQuantities)
                .canceledAmount(canceledAmount)
                .build(),
            idempotencyKey,
        )
    }
}

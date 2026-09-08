package io.playground.orderservice.application.saga.usecase

import io.playground.orderservice.application.eventstream.EventProducerPort
import io.playground.orderservice.application.eventstream.OrderEvent
import io.playground.orderservice.application.order.usecase.OrderService
import io.playground.orderservice.application.saga.dto.ClientDto
import io.playground.orderservice.application.saga.port.client.CatalogClientPort
import io.playground.orderservice.application.saga.port.client.InventoryClientPort
import io.playground.orderservice.application.saga.port.client.PaymentClientPort
import io.playground.orderservice.application.saga.port.persistence.ProcessSagaPersistencePort
import io.playground.orderservice.domain.saga.ProcessSaga
import io.playground.orderservice.exception.BusinessDetailException
import io.playground.orderservice.exception.BusinessErrorCode
import io.playground.orderservice.exception.BusinessErrorDto
import io.playground.orderservice.infrastructure.util.JsonUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.function.Supplier

@Service
class ProcessStepService(
    private val sagaPersistence: ProcessSagaPersistencePort,
    private val orderService: OrderService,
    private val catalogClient: CatalogClientPort,
    private val inventoryClient: InventoryClientPort,
    private val paymentClient: PaymentClientPort,
    private val eventProducer: EventProducerPort,
    private val jsonUtil: JsonUtil,
) {
    private fun execute(
        action: Runnable,
        saga: ProcessSaga,
        successStatus: ProcessSaga.ProcessSagaStatus,
        beforeStatus: ProcessSaga.ProcessSagaStatus,
        errorCode: BusinessErrorCode,
    ) {
        try {
            action.run()

            if (sagaPersistence.updateStatus(saga.orderExternalId, successStatus, beforeStatus)) {
                saga.updateStatus(successStatus)
            }
        } catch (e: BusinessDetailException) {
            if (sagaPersistence.updateStatus(
                    saga.orderExternalId,
                    ProcessSaga.ProcessSagaStatus.COMPENSATED,
                    beforeStatus,
                )
            ) {
                saga.updateStatus(ProcessSaga.ProcessSagaStatus.COMPENSATED)
            }

            throw BusinessDetailException(errorCode, jsonUtil.toJson(BusinessErrorDto.from(e)))
        }
    }

    private fun <T> execute(
        action: Supplier<T>,
        saga: ProcessSaga,
        successStatus: ProcessSaga.ProcessSagaStatus,
        beforeStatus: ProcessSaga.ProcessSagaStatus,
        errorCode: BusinessErrorCode,
    ): T {
        return try {
            val result = action.get()

            if (sagaPersistence.updateStatus(saga.orderExternalId, successStatus, beforeStatus)) {
                saga.updateStatus(successStatus)
            }

            result
        } catch (e: BusinessDetailException) {
            if (sagaPersistence.updateStatus(
                    saga.orderExternalId,
                    ProcessSaga.ProcessSagaStatus.COMPENSATED,
                    beforeStatus,
                )
            ) {
                saga.updateStatus(ProcessSaga.ProcessSagaStatus.COMPENSATED)
            }

            throw BusinessDetailException(errorCode, jsonUtil.toJson(BusinessErrorDto.from(e)))
        }
    }

    fun reserveStocks(
        saga: ProcessSaga,
        orderExternalId: String,
        variantQuantities: Map<Long, Int>,
    ) {
        execute(
            Runnable {
                inventoryClient.reserveStocks(
                    orderExternalId,
                    variantQuantities.entries.map { entry ->
                        ClientDto.ReservationRequest.builder()
                            .variantId(entry.key)
                            .quantity(entry.value)
                            .build()
                    },
                )
            },
            saga,
            ProcessSaga.ProcessSagaStatus.STOCKS_RESERVED,
            ProcessSaga.ProcessSagaStatus.STARTED,
            BusinessErrorCode.ORDER_PROCESS_FAILED,
        )
    }

    fun getSnapshots(
        saga: ProcessSaga,
        variantIds: List<Long>,
    ): List<ClientDto.Snapshot> = execute(
        Supplier { catalogClient.getSnapshots(variantIds) },
        saga,
        ProcessSaga.ProcessSagaStatus.STOCKS_RESERVED,
        ProcessSaga.ProcessSagaStatus.STOCKS_RESERVED,
        BusinessErrorCode.ORDER_PROCESS_FAILED,
    )

    fun doOrder(
        saga: ProcessSaga,
        userId: Long,
        orderExternalId: String,
        variantQuantities: Map<Long, Int>,
        snapshots: List<ClientDto.Snapshot>,
    ) {
        execute(
            Runnable {
                orderService.createOrder(
                    userId,
                    orderExternalId,
                    variantQuantities,
                    snapshots,
                )
            },
            saga,
            ProcessSaga.ProcessSagaStatus.ORDER_CREATED,
            ProcessSaga.ProcessSagaStatus.STOCKS_RESERVED,
            BusinessErrorCode.ORDER_PROCESS_FAILED,
        )
    }

    fun validatePayment(orderExternalId: String, amount: BigDecimal) {
        orderService.validatePayment(orderExternalId, amount)
    }

    fun approvePayment(
        saga: ProcessSaga,
        idempotencyKey: String,
        orderExternalId: String,
        paymentKey: String,
        amount: BigDecimal,
    ) {
        execute(
            Runnable {
                paymentClient.approvePayment(
                    ClientDto.ApprovePaymentRequest.builder()
                        .idempotencyKey(idempotencyKey)
                        .orderExternalId(orderExternalId)
                        .paymentKey(paymentKey)
                        .amount(amount)
                        .build(),
                )
            },
            saga,
            ProcessSaga.ProcessSagaStatus.PAYMENT_COMPLETED,
            ProcessSaga.ProcessSagaStatus.ORDER_CREATED,
            BusinessErrorCode.ORDER_PAYMENT_FAILED,
        )
    }

    fun confirmStocks(saga: ProcessSaga, orderExternalId: String) {
        execute(
            Runnable { inventoryClient.confirmStocks(orderExternalId) },
            saga,
            ProcessSaga.ProcessSagaStatus.STOCKS_CONFIRMED,
            ProcessSaga.ProcessSagaStatus.PAYMENT_COMPLETED,
            BusinessErrorCode.ORDER_PAYMENT_FAILED,
        )
    }

    fun completeOrder(saga: ProcessSaga, orderExternalId: String) {
        execute(
            Runnable {
                if (!orderService.completeOrder(orderExternalId)) {
                    throw BusinessDetailException(
                        BusinessErrorCode.ORDER_PAYMENT_FAILED,
                        "INVALID_ORDER_STATUS",
                    )
                }
            },
            saga,
            ProcessSaga.ProcessSagaStatus.ORDER_COMPLETED,
            ProcessSaga.ProcessSagaStatus.STOCKS_CONFIRMED,
            BusinessErrorCode.ORDER_PAYMENT_FAILED,
        )
    }

    @Transactional
    fun findExpiredSagas(limitSize: Int, retryMax: Int): List<ProcessSaga> {
        val expiredSagas = sagaPersistence.findExpiredSagas(limitSize, retryMax)

        expiredSagas.forEach { saga ->
            saga.updateLockedUntil(
                Instant.now().plus(Duration.ofMinutes(1)),
            )
        }

        return expiredSagas
    }

    @Transactional
    fun markCompensated(saga: ProcessSaga) {
        sagaPersistence.updateStatus(
            requireNotNull(saga.id) { "saga.id is required" },
            ProcessSaga.ProcessSagaStatus.COMPENSATED,
        )
    }

    @Transactional
    fun markRetryOrFail(saga: ProcessSaga, retryCount: Int) {
        if (saga.retryCount < retryCount) {
            sagaPersistence.updateRetryCountAndLockedUntil(
                requireNotNull(saga.id) { "saga.id is required" },
                null,
            )
            return
        }

        eventProducer.produce(
            OrderEvent.EventType.COMPENSATION_FAILED,
            OrderEvent.CompensationFailed.builder()
                .processSagaId(saga.id)
                .orderExternalId(saga.orderExternalId)
                .build(),
            UUID.randomUUID().toString(),
        )
    }

    fun compensate(idempotencyKey: String, saga: ProcessSaga) {
        if (saga.status == ProcessSaga.ProcessSagaStatus.ORDER_COMPLETED ||
            saga.status == ProcessSaga.ProcessSagaStatus.COMPENSATED
        ) {
            return
        }

        when (saga.status) {
            ProcessSaga.ProcessSagaStatus.STOCKS_CONFIRMED,
            ProcessSaga.ProcessSagaStatus.PAYMENT_COMPLETED,
            ProcessSaga.ProcessSagaStatus.ORDER_CREATED,
            -> {
                if (saga.paymentKey != null) {
                    try {
                        paymentClient.cancelPayment(
                            ClientDto.CancelPaymentRequest.builder()
                                .idempotencyKey(idempotencyKey)
                                .paymentKey(saga.paymentKey)
                                .reason("Failed to process order")
                                .build(),
                        )
                    } catch (e: BusinessDetailException) {
                        if (e.detail != "PAYMENT_NOT_FOUND") {
                            throw BusinessDetailException(
                                BusinessErrorCode.PAYMENT_SERVICE_FAILED,
                                jsonUtil.toJson(BusinessErrorDto.from(e)),
                            )
                        }
                    }
                }

                orderService.failOrder(saga.orderExternalId)
                eventProducer.produce(
                    OrderEvent.EventType.ORDER_EXPIRED,
                    OrderEvent.OrderExpired.builder()
                        .orderExternalId(saga.orderExternalId)
                        .build(),
                    idempotencyKey,
                )
            }
            ProcessSaga.ProcessSagaStatus.STOCKS_RESERVED,
            -> {
                orderService.failOrder(saga.orderExternalId)
                eventProducer.produce(
                    OrderEvent.EventType.ORDER_EXPIRED,
                    OrderEvent.OrderExpired.builder()
                        .orderExternalId(saga.orderExternalId)
                        .build(),
                    idempotencyKey,
                )
            }
            else -> {
                eventProducer.produce(
                    OrderEvent.EventType.ORDER_EXPIRED,
                    OrderEvent.OrderExpired.builder()
                        .orderExternalId(saga.orderExternalId)
                        .build(),
                    idempotencyKey,
                )
            }
        }
    }
}

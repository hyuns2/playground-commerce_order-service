package io.playground.orderservice.application.saga.usecase

import io.playground.orderservice.application.saga.dto.ClientDto
import io.playground.orderservice.application.saga.port.persistence.ProcessSagaPersistencePort
import io.playground.orderservice.domain.saga.ProcessSaga
import io.playground.orderservice.exception.BusinessDetailException
import io.playground.orderservice.exception.BusinessErrorCode
import io.playground.orderservice.exception.BusinessException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant

@Service
class ProcessSagaService(
    private val sagaPersistence: ProcessSagaPersistencePort,
    private val processStepService: ProcessStepService,
) {
    @Retryable(
        noRetryFor = [BusinessDetailException::class],
        backoff = Backoff(delay = 1000, multiplier = 2.0),
    )
    fun processOrder(
        userId: Long,
        orderExternalId: String,
        variantQuantities: Map<Long, Int>,
    ) {
        val saga = sagaPersistence.findByOrderExternalId(orderExternalId)
            .orElseGet {
                sagaPersistence.saveAndFlush(
                    ProcessSaga.of(
                        null,
                        orderExternalId,
                        null,
                        ProcessSaga.ProcessSagaStatus.STARTED,
                        Instant.now().plus(Duration.ofMinutes(5)),
                        0,
                        null,
                    ),
                )
            }

        if (saga.status == ProcessSaga.ProcessSagaStatus.COMPENSATED) {
            throw BusinessDetailException(
                BusinessErrorCode.ORDER_PROCESS_FAILED,
                "ORDER_STATUS: FAILED",
            )
        }

        if (saga.status != ProcessSaga.ProcessSagaStatus.STARTED &&
            saga.status != ProcessSaga.ProcessSagaStatus.STOCKS_RESERVED &&
            saga.status != ProcessSaga.ProcessSagaStatus.ORDER_CREATED
        ) {
            return
        }

        if (saga.status == ProcessSaga.ProcessSagaStatus.STARTED) {
            processStepService.reserveStocks(saga, orderExternalId, variantQuantities)
        }

        if (saga.status == ProcessSaga.ProcessSagaStatus.STOCKS_RESERVED) {
            val infos: List<ClientDto.Snapshot> = processStepService.getSnapshots(
                saga,
                variantQuantities.keys.toList(),
            )

            processStepService.doOrder(
                saga,
                userId,
                orderExternalId,
                variantQuantities,
                infos,
            )
        }

        if (saga.status != ProcessSaga.ProcessSagaStatus.ORDER_CREATED) {
            throw BusinessException(BusinessErrorCode.ORDER_PROCESS_FAILED)
        }
    }

    @Retryable(
        noRetryFor = [BusinessDetailException::class],
        backoff = Backoff(delay = 1000, multiplier = 2.0),
    )
    fun processPayment(
        idempotencyKey: String,
        orderExternalId: String,
        paymentKey: String,
        amount: BigDecimal,
    ) {
        processStepService.validatePayment(orderExternalId, amount)

        val saga = sagaPersistence.findByOrderExternalId(orderExternalId)
            .orElseThrow {
                BusinessDetailException(
                    BusinessErrorCode.ORDER_PAYMENT_FAILED,
                    "ORDER_PROCESS_FAILED",
                )
            }

        if (saga.status == ProcessSaga.ProcessSagaStatus.COMPENSATED) {
            throw BusinessDetailException(
                BusinessErrorCode.ORDER_PAYMENT_FAILED,
                "ORDER_STATUS: FAILED",
            )
        }

        if (saga.status == ProcessSaga.ProcessSagaStatus.STARTED ||
            saga.status == ProcessSaga.ProcessSagaStatus.STOCKS_RESERVED
        ) {
            throw BusinessDetailException(
                BusinessErrorCode.ORDER_PAYMENT_FAILED,
                "ORDER_PROCESS_FAILED",
            )
        }

        if (!sagaPersistence.updatePaymentKey(orderExternalId, paymentKey)) {
            throw BusinessDetailException(
                BusinessErrorCode.ORDER_PAYMENT_FAILED,
                "PAYMENT_UPDATE_FAILED",
            )
        }

        if (saga.status == ProcessSaga.ProcessSagaStatus.ORDER_CREATED) {
            processStepService.approvePayment(
                saga,
                idempotencyKey,
                orderExternalId,
                paymentKey,
                amount,
            )
        }

        if (saga.status == ProcessSaga.ProcessSagaStatus.PAYMENT_COMPLETED) {
            processStepService.confirmStocks(saga, orderExternalId)
        }

        if (saga.status == ProcessSaga.ProcessSagaStatus.STOCKS_CONFIRMED) {
            processStepService.completeOrder(saga, orderExternalId)
        }

        if (saga.status != ProcessSaga.ProcessSagaStatus.ORDER_COMPLETED) {
            throw BusinessException(BusinessErrorCode.ORDER_PAYMENT_FAILED)
        }
    }
}

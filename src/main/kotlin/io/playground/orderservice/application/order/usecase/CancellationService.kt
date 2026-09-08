package io.playground.orderservice.application.order.usecase

import io.playground.orderservice.application.saga.dto.ClientDto
import io.playground.orderservice.application.saga.port.client.PaymentClientPort
import io.playground.orderservice.exception.BusinessDetailException
import io.playground.orderservice.exception.BusinessErrorCode
import io.playground.orderservice.exception.BusinessErrorDto
import io.playground.orderservice.infrastructure.util.JsonUtil
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class CancellationService(
    private val paymentClient: PaymentClientPort,
    private val cancellationTxService: CancellationTxService,
    private val jsonUtil: JsonUtil,
) {
    @Retryable(
        noRetryFor = [BusinessDetailException::class],
        backoff = Backoff(delay = 1000, multiplier = 2.0),
    )
    fun cancelAll(
        idempotencyKey: String,
        orderExternalId: String,
        paymentKey: String,
        reason: String,
    ) {
        cancellationTxService.validateToCancelAll(orderExternalId)

        try {
            paymentClient.cancelPayment(
                ClientDto.CancelPaymentRequest.builder()
                    .idempotencyKey(idempotencyKey)
                    .paymentKey(paymentKey)
                    .reason(reason)
                    .build(),
            )
        } catch (e: BusinessDetailException) {
            throw BusinessDetailException(
                BusinessErrorCode.ORDER_CANCELLATION_FAILED,
                jsonUtil.toJson(BusinessErrorDto.from(e)),
            )
        }

        cancellationTxService.cancelAll(idempotencyKey, orderExternalId, reason)
    }

    @Retryable(
        noRetryFor = [BusinessDetailException::class],
        backoff = Backoff(delay = 1000, multiplier = 2.0),
    )
    fun cancelPartially(
        idempotencyKey: String,
        orderExternalId: String,
        paymentKey: String,
        variantQuantities: Map<Long, Int>,
        reason: String,
    ) {
        val cancelsAmount: BigDecimal = cancellationTxService
            .validateAndCalculateAmountToCancelPartially(orderExternalId, variantQuantities)

        try {
            paymentClient.cancelPartially(
                ClientDto.CancelPartiallyRequest.builder()
                    .idempotencyKey(idempotencyKey)
                    .paymentKey(paymentKey)
                    .amount(cancelsAmount)
                    .reason(reason)
                    .build(),
            )
        } catch (e: BusinessDetailException) {
            throw BusinessDetailException(
                BusinessErrorCode.ORDER_PARTIAL_CANCELLATION_FAILED,
                jsonUtil.toJson(BusinessErrorDto.from(e)),
            )
        }

        cancellationTxService.cancelPartially(
            idempotencyKey,
            orderExternalId,
            variantQuantities,
            reason,
            cancelsAmount,
        )
    }
}

package io.playground.orderservice.infrastructure.client

import feign.FeignException
import io.playground.orderservice.application.saga.dto.ClientDto
import io.playground.orderservice.application.saga.port.client.PaymentClientPort
import io.playground.orderservice.exception.BusinessDetailException
import io.playground.orderservice.exception.BusinessErrorCode
import io.playground.orderservice.infrastructure.util.JsonUtil
import org.springframework.stereotype.Component

@Component
class PaymentClientAdapter(
    private val paymentClient: PaymentFeignClient,
    private val jsonUtil: JsonUtil,
) : PaymentClientPort {
    private fun execute(action: () -> Unit) {
        try {
            action()
        } catch (fe: FeignException) {
            if (jsonUtil.isBusinessDetailError(fe.contentUTF8())) {
                throw BusinessDetailException(BusinessErrorCode.PAYMENT_SERVICE_FAILED, fe.contentUTF8())
            }
            fe.printStackTrace()
            throw fe
        }
    }

    override fun approvePayment(request: ClientDto.ApprovePaymentRequest) {
        execute { paymentClient.approvePayment(request) }
    }

    override fun cancelPayment(request: ClientDto.CancelPaymentRequest) {
        execute { paymentClient.cancelPayment(request) }
    }

    override fun cancelPartially(request: ClientDto.CancelPartiallyRequest) {
        execute { paymentClient.cancelPartially(request) }
    }
}

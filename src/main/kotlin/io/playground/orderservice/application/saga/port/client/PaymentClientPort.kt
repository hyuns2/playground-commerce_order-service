package io.playground.orderservice.application.saga.port.client

import io.playground.orderservice.application.saga.dto.ClientDto

interface PaymentClientPort {
    fun approvePayment(request: ClientDto.ApprovePaymentRequest)

    fun cancelPayment(request: ClientDto.CancelPaymentRequest)

    fun cancelPartially(request: ClientDto.CancelPartiallyRequest)
}

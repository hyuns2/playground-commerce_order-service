package io.playground.orderservice.application.saga.port.client;

import io.playground.orderservice.application.saga.dto.ClientDto;

public interface PaymentClientPort {
    void approvePayment(ClientDto.ApprovePaymentRequest request);

    void cancelPayment(ClientDto.CancelPaymentRequest request);

    void cancelPartially(ClientDto.CancelPartiallyRequest request);
}

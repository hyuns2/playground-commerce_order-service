package io.playground.orderservice.infrastructure.client;

import feign.FeignException;
import io.playground.orderservice.application.saga.dto.ClientDto;
import io.playground.orderservice.application.saga.port.client.PaymentClientPort;
import io.playground.orderservice.exception.BusinessDetailException;
import io.playground.orderservice.exception.BusinessErrorCode;
import io.playground.orderservice.infrastructure.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentClientAdapter implements PaymentClientPort {
     private final PaymentFeignClient paymentClient;
    private final JsonUtil jsonUtil;

    private void execute(Runnable action) {
        try {
            action.run();
        } catch (FeignException fe) {
            if (jsonUtil.isBusinessDetailError(fe.contentUTF8()))
                throw new BusinessDetailException(
                        BusinessErrorCode.PAYMENT_SERVICE_FAILED,
                        fe.contentUTF8()
                );

            fe.printStackTrace();
            throw fe;
        }
    }

    @Override
    public void approvePayment(ClientDto.ApprovePaymentRequest request) {
        execute(() -> paymentClient
                .approvePayment(request));
    }

    @Override
    public void cancelPayment(ClientDto.CancelPaymentRequest request) {
        execute(() -> paymentClient
                .cancelPayment(request));
    }

    @Override
    public void cancelPartially(ClientDto.CancelPartiallyRequest request) {
        execute(() -> paymentClient
                .cancelPartially(request));
    }
}

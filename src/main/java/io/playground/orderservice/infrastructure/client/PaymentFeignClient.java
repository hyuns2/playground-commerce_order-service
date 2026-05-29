package io.playground.orderservice.infrastructure.client;

import io.playground.orderservice.application.saga.dto.ClientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "paymentClient",
        url = "${api.internal.payment-service.url}"
)
public interface PaymentFeignClient {
    @PostMapping("/approve")
    ResponseEntity<Void> approvePayment(@RequestBody ClientDto.ApprovePaymentRequest request);

    @PostMapping("/cancel")
    ResponseEntity<Void> cancelPayment(@RequestBody ClientDto.CancelPaymentRequest request);

    @PostMapping("/partial-cancel")
    ResponseEntity<Void> cancelPartially(@RequestBody ClientDto.CancelPartiallyRequest request);
}

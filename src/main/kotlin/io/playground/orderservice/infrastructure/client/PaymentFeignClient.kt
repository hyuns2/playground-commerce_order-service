package io.playground.orderservice.infrastructure.client

import io.playground.orderservice.application.saga.dto.ClientDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "paymentClient",
    url = "\${api.internal.payment-service.url}",
)
interface PaymentFeignClient {
    @PostMapping("/approve")
    fun approvePayment(@RequestBody request: ClientDto.ApprovePaymentRequest): ResponseEntity<Void>

    @PostMapping("/cancel")
    fun cancelPayment(@RequestBody request: ClientDto.CancelPaymentRequest): ResponseEntity<Void>

    @PostMapping("/partial-cancel")
    fun cancelPartially(@RequestBody request: ClientDto.CancelPartiallyRequest): ResponseEntity<Void>
}

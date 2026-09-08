package io.playground.orderservice.presentation

import io.playground.orderservice.application.order.dto.OrderDto
import io.playground.orderservice.application.order.usecase.CancellationService
import io.playground.orderservice.application.order.usecase.OrderService
import io.playground.orderservice.application.saga.usecase.ProcessSagaService
import io.playground.orderservice.domain.order.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderController(
    private val orderSagaService: ProcessSagaService,
    private val orderService: OrderService,
    private val cancellationService: CancellationService,
) {
    @PostMapping
    fun doOrder(
        @RequestParam userId: Long,
        @RequestBody request: OrderDto.DoOrderRequest,
    ): ResponseEntity<Void> {
        orderSagaService.processOrder(userId, request.orderExternalId, request.variantQuantities)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/payment")
    fun doPayment(@RequestBody request: OrderDto.DoPaymentRequest): ResponseEntity<Void> {
        orderSagaService.processPayment(
            request.idempotencyKey,
            request.orderExternalId,
            request.paymentKey,
            request.amount,
        )
        return ResponseEntity.ok().build()
    }

    @GetMapping
    fun getOrder(@RequestParam orderExternalId: String): ResponseEntity<Order> =
        ResponseEntity.ok(orderService.getOrder(orderExternalId))

    @PostMapping("/cancel")
    fun cancelAll(@RequestBody request: OrderDto.CancelAllRequest): ResponseEntity<Void> {
        cancellationService.cancelAll(
            request.idempotencyKey,
            request.orderExternalId,
            request.paymentKey,
            request.reason,
        )
        return ResponseEntity.ok().build()
    }

    @PostMapping("/partial-cancel")
    fun cancelPartially(@RequestBody request: OrderDto.CancelPartiallyRequest): ResponseEntity<Void> {
        cancellationService.cancelPartially(
            request.idempotencyKey,
            request.orderExternalId,
            request.paymentKey,
            request.variantQuantities,
            request.reason,
        )
        return ResponseEntity.ok().build()
    }
}

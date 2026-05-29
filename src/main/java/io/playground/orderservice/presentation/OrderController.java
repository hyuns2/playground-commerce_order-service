package io.playground.orderservice.presentation;

import io.playground.orderservice.application.order.dto.OrderDto;
import io.playground.orderservice.application.order.usecase.CancellationService;
import io.playground.orderservice.application.order.usecase.OrderService;
import io.playground.orderservice.application.saga.usecase.ProcessSagaService;
import io.playground.orderservice.domain.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final ProcessSagaService orderSagaService;
    private final OrderService orderService;
    private final CancellationService cancellationService;

    @PostMapping
    public ResponseEntity<Void> doOrder(@RequestParam Long userId,
                                         @RequestBody OrderDto.DoOrderRequest request) {
        orderSagaService.processOrder(
                userId,
                request.orderExternalId(),
                request.variantQuantities()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/payment")
    public ResponseEntity<Void> doPayment(@RequestBody OrderDto.DoPaymentRequest request) {
        orderSagaService.processPayment(
                request.idempotencyKey(),
                request.orderExternalId(),
                request.paymentKey(),
                request.amount()
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Order> getOrder(@RequestParam String orderExternalId) {
        return ResponseEntity.ok().body(
                orderService.getOrder(orderExternalId)
        );
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelAll(@RequestBody OrderDto.CancelAllRequest request) {
        cancellationService.cancelAll(
                request.idempotencyKey(),
                request.orderExternalId(),
                request.paymentKey(),
                request.reason()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/partial-cancel")
    public ResponseEntity<Void> cancelPartially(@RequestBody OrderDto.CancelPartiallyRequest request) {
        cancellationService.cancelPartially(
                request.idempotencyKey(),
                request.orderExternalId(),
                request.paymentKey(),
                request.variantQuantities(),
                request.reason()
        );

        return ResponseEntity.ok().build();
    }
}

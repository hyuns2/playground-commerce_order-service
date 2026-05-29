package io.playground.orderservice.application.order.dto;

import java.math.BigDecimal;
import java.util.Map;

public class OrderDto {
    public record DoOrderRequest(
            String orderExternalId,
            Map<Long, Integer> variantQuantities
    ) {
    }

    public record DoPaymentRequest(
            String idempotencyKey,
            String orderExternalId,
            String paymentKey,
            BigDecimal amount
    ) {
    }

    public record CancelAllRequest(
            String idempotencyKey,
            String orderExternalId,
            String paymentKey,
            String reason
    ) {
    }

    public record CancelPartiallyRequest(
            String idempotencyKey,
            String orderExternalId,
            String paymentKey,
            Map<Long, Integer> variantQuantities,
            String reason
    ) {
    }
}

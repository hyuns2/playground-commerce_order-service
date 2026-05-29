package io.playground.orderservice.application.saga.dto;

import lombok.Builder;

import java.math.BigDecimal;

public class ClientDto {
    public record Snapshot(
            Long productId,
            String productName,
            Long variantId,
            String variantName,
            BigDecimal price
    ) {
    }

    @Builder
    public record ReservationRequest(
            Long variantId,
            Integer quantity
    ) {
    }

    @Builder
    public record ApprovePaymentRequest(
            String idempotencyKey,
            String orderExternalId,
            String paymentKey,
            BigDecimal amount
    ) {
    }

    @Builder
    public record CancelPaymentRequest(
            String idempotencyKey,
            String paymentKey,
            String reason
    ) {
    }

    @Builder
    public record CancelPartiallyRequest(
            String idempotencyKey,
            String paymentKey,
            BigDecimal amount,
            String reason
    ) {
    }
}

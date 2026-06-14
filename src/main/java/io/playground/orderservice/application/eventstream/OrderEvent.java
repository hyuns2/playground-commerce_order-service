package io.playground.orderservice.application.eventstream;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;

public class OrderEvent {
    public enum EventType {
        ORDER_EXPIRED,
        ALL_CANCELED,
        PARTIALLY_CANCELED,
        COMPENSATION_FAILED,
        OUTBOXING_FAILED
    }

    @Builder
    public record OrderExpired(
            String orderExternalId
    ) {
    }

    @Builder
    public record AllCanceled(
            String orderExternalId
    ) {
    }

    @Builder
    public record PartiallyCanceled(
            String idempotencyKey,
            String orderExternalId,
            Map<Long, Integer> canceledVariantQuantities,
            BigDecimal canceledAmount
    ) {
    }

    @Builder
    public record CompensationFailed(
            Long processSagaId,
            String orderExternalId
    ) {
    }

    @Builder
    public record OutboxingFailed(
            Long outboxId,
            String eventId,
            OrderEvent.EventType eventType
    ) {
    }
}

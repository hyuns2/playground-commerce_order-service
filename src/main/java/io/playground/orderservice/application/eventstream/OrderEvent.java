package io.playground.orderservice.application.eventstream;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;

public class OrderEvent {
    public enum EventType {
        ORDER_EXPIRED,
        CANCEL_ALL,
        CANCEL_PARTIALLY
    }

    @Builder
    public record OrderExpired(
            String orderExternalId
    ) {
    }

    @Builder
    public record CancelAll(
            String orderExternalId
    ) {
    }

    @Builder
    public record CancelPartially(
            String idempotencyKey,
            String orderExternalId,
            Map<Long, Integer> canceledVariantQuantities,
            BigDecimal canceledAmount
    ) {
    }
}

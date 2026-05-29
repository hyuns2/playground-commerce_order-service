package io.playground.orderservice.application.order.dto;

import io.playground.orderservice.domain.order.Order;

import java.math.BigDecimal;

public record OrderAmountInfo(
        Long orderId,
        Order.OrderStatus status,
        BigDecimal finalAmount
) {
}

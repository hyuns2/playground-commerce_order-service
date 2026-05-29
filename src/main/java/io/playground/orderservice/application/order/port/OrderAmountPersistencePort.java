package io.playground.orderservice.application.order.port;

import io.playground.orderservice.application.order.dto.OrderAmountInfo;
import io.playground.orderservice.domain.order.OrderAmount;

import java.util.Optional;

public interface OrderAmountPersistencePort {
    Optional<OrderAmountInfo> findOrderAmountInfoByOrderExternalId(String orderExternalId);

    OrderAmount save(OrderAmount orderAmount);
}

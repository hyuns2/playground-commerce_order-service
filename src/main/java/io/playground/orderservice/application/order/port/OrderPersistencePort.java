package io.playground.orderservice.application.order.port;

import io.playground.orderservice.domain.order.Order;

import java.util.List;
import java.util.Optional;

public interface OrderPersistencePort {
    Optional<Order> findByExternalId(String externalId);

    Order save(Order order);

    boolean updateStatusByExternalId(String externalId,
                                     Order.OrderStatus status,
                                     List<Order.OrderStatus> beforeStatuses);
}

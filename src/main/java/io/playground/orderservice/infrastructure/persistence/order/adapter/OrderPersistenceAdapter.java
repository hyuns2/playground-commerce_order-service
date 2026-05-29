package io.playground.orderservice.infrastructure.persistence.order.adapter;

import io.playground.orderservice.application.order.port.OrderPersistencePort;
import io.playground.orderservice.domain.order.Order;
import io.playground.orderservice.infrastructure.persistence.order.entity.OrderEntity;
import io.playground.orderservice.infrastructure.persistence.order.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderPersistencePort {
    private final OrderJpaRepository orderRepository;

    @Override
    public Optional<Order> findByExternalId(String externalId) {
        return orderRepository.findByExternalId(externalId)
                .map(OrderEntity::toDomain);
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(
                OrderEntity.fromDomain(order)
        ).toDomain();
    }

    @Override
    public boolean updateStatusByExternalId(String externalId,
                                            Order.OrderStatus status,
                                            List<Order.OrderStatus> beforeStatuses) {
        return beforeStatuses == null || beforeStatuses.isEmpty() ?
                orderRepository.updateStatusByExternalId(
                        externalId, status
                ) > 0 :
                orderRepository.updateStatusByExternalId(
                        externalId, status, beforeStatuses
                ) > 0;
    }
}

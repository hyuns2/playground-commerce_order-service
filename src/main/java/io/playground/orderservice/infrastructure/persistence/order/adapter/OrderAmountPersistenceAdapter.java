package io.playground.orderservice.infrastructure.persistence.order.adapter;

import io.playground.orderservice.application.order.dto.OrderAmountInfo;
import io.playground.orderservice.application.order.port.OrderAmountPersistencePort;
import io.playground.orderservice.domain.order.OrderAmount;
import io.playground.orderservice.infrastructure.persistence.order.entity.OrderAmountEntity;
import io.playground.orderservice.infrastructure.persistence.order.repository.OrderAmountJpaRepository;
import io.playground.orderservice.infrastructure.persistence.order.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderAmountPersistenceAdapter implements OrderAmountPersistencePort {
    private final OrderAmountJpaRepository orderAmountRepository;
    private final OrderJpaRepository orderRepository;

    @Override
    public Optional<OrderAmountInfo> findOrderAmountInfoByOrderExternalId(String orderExternalId) {
        return orderAmountRepository
                .findOrderAmountInfoByOrderExternalId(
                        orderExternalId
                );
    }

    @Override
    public OrderAmount save(OrderAmount orderAmount) {
        return orderAmountRepository.save(
                OrderAmountEntity.fromDomain(
                        orderAmount,
                        orderRepository.getReferenceById(orderAmount.getOrderId())
                )
        ).toDomain();
    }
}

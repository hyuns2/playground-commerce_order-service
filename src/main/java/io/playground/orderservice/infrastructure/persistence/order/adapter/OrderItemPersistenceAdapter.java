package io.playground.orderservice.infrastructure.persistence.order.adapter;

import io.playground.orderservice.application.order.port.OrderItemPersistencePort;
import io.playground.orderservice.domain.order.Order;
import io.playground.orderservice.domain.order.OrderItem;
import io.playground.orderservice.infrastructure.persistence.order.entity.OrderEntity;
import io.playground.orderservice.infrastructure.persistence.order.entity.OrderItemEntity;
import io.playground.orderservice.infrastructure.persistence.order.repository.OrderItemJdbcTemplate;
import io.playground.orderservice.infrastructure.persistence.order.repository.OrderItemJpaRepository;
import io.playground.orderservice.infrastructure.persistence.order.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderItemPersistenceAdapter implements OrderItemPersistencePort {
    private final OrderItemJpaRepository orderItemRepository;
    private final OrderItemJdbcTemplate orderItemTemplate;
    private final OrderJpaRepository orderRepository;

    @Override
    public List<OrderItem> findAllByOrderExternalIdAndOrderStatus(String orderExternalId,
                                                                  Order.OrderStatus orderStatus) {
        return orderItemRepository
                .findAllByOrder_ExternalIdAndOrder_Status(
                        orderExternalId,
                        Order.OrderStatus.PAID
                ).stream()
                .map(OrderItemEntity::toDomain)
                .toList();
    }

    @Override
    public List<OrderItem> findAllByVariantIdsAndOrderExternalIdAndOrderStatusIn(String orderExternalId,
                                                                                 List<Long> variantIds,
                                                                                 List<Order.OrderStatus> orderStatuses) {
        return orderItemRepository
                .findAllByVariantIdInAndOrder_ExternalIdAndOrder_StatusIn(
                        variantIds,
                        orderExternalId,
                        orderStatuses
                ).stream()
                .map(OrderItemEntity::toDomain)
                .toList();
    }

    @Override
    public void saveAll(List<OrderItem> orderItems) {
        OrderEntity orderReference = orderRepository
                .getReferenceById(orderItems.get(0).getOrderId());

        orderItemRepository.saveAll(
                orderItems.stream()
                        .map(entity ->
                                OrderItemEntity.fromDomain(
                                        entity,
                                        orderReference
                                )
                        )
                        .toList()
        );
    }

    @Override
    public boolean updateCanceledReasonsToCancelAll(String orderExternalId,
                                                    String reason) {
        return orderItemRepository
                .updateCanceledReasonsToCancelAll(
                        orderExternalId, reason
                ) > 0;
    }

    @Override
    public boolean updateCanceledInfosToCancelPartially(String orderExternalId,
                                                        Map<Long, Integer> cancelsItemQuantities,
                                                        String reason) {
        return Arrays.stream(
                orderItemTemplate
                        .updateCanceledInfosToCancelPartially(
                                orderExternalId,
                                cancelsItemQuantities,
                                reason
                        )
        ).allMatch(v -> v > 0);
    }
}

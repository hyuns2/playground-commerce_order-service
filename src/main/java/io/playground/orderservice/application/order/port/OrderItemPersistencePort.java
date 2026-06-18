package io.playground.orderservice.application.order.port;

import io.playground.orderservice.domain.order.Order;
import io.playground.orderservice.domain.order.OrderItem;

import java.util.List;
import java.util.Map;

public interface OrderItemPersistencePort {
    List<OrderItem> findAllByOrderExternalIdAndOrderStatus(String orderExternalId,
                                                           Order.OrderStatus orderStatus);
    List<OrderItem> findAllByVariantIdsAndOrderExternalIdAndOrderStatusIn(String orderExternalId,
                                                                          List<Long> variantIds,
                                                                          List<Order.OrderStatus> orderStatuses);

    void saveAll(List<OrderItem> orderItems);

    boolean updateCanceledReasonsToCancelAll(String orderExternalId,
                                             String reason);

    boolean updateCanceledInfosToCancelPartially(String orderExternalId,
                                                 Map<Long, Integer> cancelsItemQuantities,
                                                 String reason);
}

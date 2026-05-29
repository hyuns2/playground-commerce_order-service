package io.playground.orderservice.application.order.port;

import io.playground.orderservice.domain.order.OrderItem;

import java.util.List;
import java.util.Map;

public interface OrderItemPersistencePort {
    List<OrderItem> findAllByVariantIdsAndOrderExternalId(List<Long> variantIds,
                                                          String orderExternalId);

    void saveAll(List<OrderItem> orderItems);

    boolean updateCanceledReasonsByOrderExternalId(String orderExternalId, String reason);

    boolean updateCanceledQuantityAndReasonsByVariantIds(Map<Long, Integer> cancelsItemQuantities, String reason);
}

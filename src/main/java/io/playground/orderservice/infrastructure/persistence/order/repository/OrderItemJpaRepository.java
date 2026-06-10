package io.playground.orderservice.infrastructure.persistence.order.repository;

import io.playground.orderservice.infrastructure.persistence.order.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemEntity, Long> {
    List<OrderItemEntity> findAllByVariantIdInAndOrder_ExternalId(List<Long> variantIds,
                                                                  String orderExternalId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
            "update OrderItemEntity oi " +
                    "set oi.canceledQuantity = oi.quantity, " +
                    "oi.canceledReason = :reason " +
            "where oi.order.externalId = :orderExternalId and " +
                    "oi.canceledQuantity = 0"
    )
    int updateCanceledReasonsByOrderExternalId(String orderExternalId, String reason);
}

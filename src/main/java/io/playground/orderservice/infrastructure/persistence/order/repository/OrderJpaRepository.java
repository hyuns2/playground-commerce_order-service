package io.playground.orderservice.infrastructure.persistence.order.repository;

import io.playground.orderservice.domain.order.Order;
import io.playground.orderservice.infrastructure.persistence.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByExternalId(String externalId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
            "update OrderEntity o " +
                "set o.status = :status " +
            "where o.externalId = :externalId"
    )
    int updateStatusByExternalId(String externalId,
                                 Order.OrderStatus status);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
            "update OrderEntity o " +
                    "set o.status = :status " +
            "where o.externalId = :externalId " +
                    "and o.status in :beforeStatuses"
    )
    int updateStatusByExternalId(String externalId,
                                 Order.OrderStatus status,
                                 List<Order.OrderStatus> beforeStatuses);
}

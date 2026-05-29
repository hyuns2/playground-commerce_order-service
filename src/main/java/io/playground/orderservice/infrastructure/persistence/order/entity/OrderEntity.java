package io.playground.orderservice.infrastructure.persistence.order.entity;

import io.playground.orderservice.domain.order.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String externalId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Order.OrderStatus status;

    public static OrderEntity fromDomain(Order order) {
        return OrderEntity.builder()
                .externalId(order.getExternalId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .build();
    }

    public Order toDomain() {
        return Order.of(
                this.id,
                this.externalId,
                this.userId,
                this.status
        );
    }
}

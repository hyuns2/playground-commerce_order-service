package io.playground.orderservice.infrastructure.persistence.order.entity;

import io.playground.orderservice.domain.order.OrderItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "order_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_variantId_orderId",
                        columnNames = {"variantId", "orderId"}
                )
        }
)
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",nullable = false)
    private OrderEntity order;

    @Column(nullable = false)
    private Long variantId;

    @Column(nullable = false)
    private String variantName;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderItem.OrderItemStatus status;

    @Column(nullable = false)
    private int canceledQuantity;

    @Column
    private String canceledReason;

    public static OrderItemEntity fromDomain(OrderItem orderItem,
                                             OrderEntity orderEntity) {
        return OrderItemEntity.builder()
                .order(orderEntity)
                .variantId(orderItem.getVariantId())
                .variantName(orderItem.getVariantName())
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductName())
                .price(orderItem.getPrice())
                .quantity(orderItem.getQuantity())
                .status(orderItem.getStatus())
                .canceledQuantity(orderItem.getCanceledQuantity())
                .canceledReason(orderItem.getCanceledReason())
                .build();
    }

    public OrderItem toDomain() {
        return OrderItem.of(
                this.id,
                this.order.getId(),
                this.variantId,
                this.variantName,
                this.productId,
                this.productName,
                this.price,
                this.quantity,
                this.status,
                this.canceledQuantity,
                this.canceledReason
        );
    }
}

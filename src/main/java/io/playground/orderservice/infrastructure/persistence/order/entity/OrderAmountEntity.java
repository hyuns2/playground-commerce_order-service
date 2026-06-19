package io.playground.orderservice.infrastructure.persistence.order.entity;

import io.playground.orderservice.domain.order.OrderAmount;
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
        name = "order_amounts",
        indexes = {
                @Index(name = "idx_finalAmount", columnList = "finalAmount")
        }
)
public class OrderAmountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private BigDecimal discountAmount;

    @Column(nullable = false)
    private BigDecimal deliveryFee;

    @Column(nullable = false)
    private BigDecimal finalAmount;

    public static OrderAmountEntity fromDomain(OrderAmount orderAmount,
                                               OrderEntity orderEntity) {
        return OrderAmountEntity.builder()
                .order(orderEntity)
                .totalAmount(orderAmount.getTotalAmount())
                .discountAmount(orderAmount.getDiscountAmount())
                .deliveryFee(orderAmount.getDeliveryFee())
                .finalAmount(orderAmount.getFinalAmount())
                .build();
    }

    public OrderAmount toDomain() {
        return OrderAmount.of(
                this.id,
                this.order.getId(),
                this.totalAmount,
                this.discountAmount,
                this.deliveryFee,
                this.finalAmount
        );
    }
}

package io.playground.orderservice.domain.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OrderAmount {
    private Long id;

    private Long orderId;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal deliveryFee;

    private BigDecimal finalAmount;

    public static OrderAmount of(Long id,
                                 Long orderId,
                                 BigDecimal totalAmount,
                                 BigDecimal discountAmount,
                                 BigDecimal deliveryFee,
                                 BigDecimal finalAmount) {
        return new OrderAmount(id, orderId, totalAmount, discountAmount, deliveryFee, finalAmount);
    }
}

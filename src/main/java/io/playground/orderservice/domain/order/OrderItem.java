package io.playground.orderservice.domain.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OrderItem {
    private Long id;

    private Long orderId;

    private Long variantId;

    private String variantName;

    private Long productId;

    private String productName;

    private BigDecimal price;

    private int quantity;

    private OrderItemStatus status;

    private int canceledQuantity;

    private String canceledReason;

    public enum OrderItemStatus {
        NONE, PREPARING, DELIVERED, COMPLETED
    }

    public static OrderItem of(Long id,
                               Long orderId,
                               Long variantId,
                               String variantName,
                               Long productId,
                               String productName,
                               BigDecimal price,
                               int quantity,
                               OrderItemStatus status,
                               int canceledQuantity,
                               String canceledReason) {
        return new OrderItem(id, orderId, variantId, variantName, productId, productName, price, quantity, status, canceledQuantity, canceledReason);
    }
}

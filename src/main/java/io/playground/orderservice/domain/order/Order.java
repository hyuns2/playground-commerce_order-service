package io.playground.orderservice.domain.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Order {
    private Long id;

    private String externalId;

    private Long userId;

    private OrderStatus status;

    public enum OrderStatus {
        CREATED, PAID, COMPLETED,
        CANCELED, PARTIAL_CANCELED, FAILED
    }

    public static Order of(Long id,
                           String externalId,
                           Long userId,
                           OrderStatus status) {
        return new Order(id, externalId, userId, status);
    }
}

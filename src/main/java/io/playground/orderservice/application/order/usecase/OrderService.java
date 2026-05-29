package io.playground.orderservice.application.order.usecase;

import io.playground.orderservice.application.order.dto.OrderAmountInfo;
import io.playground.orderservice.application.order.port.OrderAmountPersistencePort;
import io.playground.orderservice.application.order.port.OrderItemPersistencePort;
import io.playground.orderservice.application.order.port.OrderPersistencePort;
import io.playground.orderservice.application.saga.dto.ClientDto;
import io.playground.orderservice.domain.order.Order;
import io.playground.orderservice.domain.order.OrderAmount;
import io.playground.orderservice.domain.order.OrderItem;
import io.playground.orderservice.exception.BusinessDetailException;
import io.playground.orderservice.exception.BusinessErrorCode;
import io.playground.orderservice.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderPersistencePort orderPersistence;
    private final OrderAmountPersistencePort orderAmountPersistence;
    private final OrderItemPersistencePort orderItemPersistence;
    private static final BigDecimal DELIVERY_FEE = BigDecimal.valueOf(3000);

    @Transactional(readOnly = true)
    public Order getOrder(String orderExternalId) {
        return orderPersistence.findByExternalId(orderExternalId)
                .orElseThrow(() -> new BusinessException(
                        BusinessErrorCode.ORDER_NOT_FOUND)
                );
    }

    @Transactional
    public Order createOrder(Long userId,
                             String orderExternalId,
                             Map<Long, Integer> variantQuantities,
                             List<ClientDto.Snapshot> snapshots) {
        Order existingOrder = getExistingOrder(orderExternalId);
        if (existingOrder != null)
            return existingOrder;

        Order order = orderPersistence.save(
                Order.of(
                        null,
                        orderExternalId,
                        userId,
                        Order.OrderStatus.CREATED
                )
        );

        List<OrderItem> orderItems = snapshots.stream()
                .map(info -> OrderItem.of(
                        null,
                        order.getId(),
                        info.variantId(),
                        info.variantName(),
                        info.productId(),
                        info.productName(),
                        info.price(),
                        variantQuantities.get(info.variantId()),
                        OrderItem.OrderItemStatus.NONE,
                        0,
                        null
                )).toList();
        orderItemPersistence.saveAll(orderItems);

        BigDecimal totalAmount = orderItems.stream()
                .map(item -> item.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
                ).reduce(BigDecimal.ZERO, BigDecimal::add);
        orderAmountPersistence.save(
                OrderAmount.of(
                        null,
                        order.getId(),
                        totalAmount,
                        BigDecimal.ZERO,
                        DELIVERY_FEE,
                        totalAmount.add(DELIVERY_FEE)
                )
        );

        return order;
    }

    @Transactional(readOnly = true)
    public Order getExistingOrder(String orderExternalId) {
        return orderPersistence
                .findByExternalId(orderExternalId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public void validatePayment(String orderExternalId,
                                BigDecimal amount) {
        OrderAmountInfo orderAmountInfo = orderAmountPersistence
                .findOrderAmountInfoByOrderExternalId(orderExternalId)
                .orElseThrow(() ->
                        new BusinessDetailException(
                                BusinessErrorCode.ORDER_PAYMENT_FAILED,
                                "ORDER_NOT_FOUND"
                        )
                );

        if (orderAmountInfo.status() !=
                Order.OrderStatus.CREATED)
            throw new BusinessDetailException(
                    BusinessErrorCode.ORDER_PAYMENT_FAILED,
                    "ORDER_STATUS: " + orderAmountInfo.status()
            );

        if (orderAmountInfo.finalAmount()
                .compareTo(amount) != 0)
            throw new BusinessDetailException(
                    BusinessErrorCode.ORDER_PAYMENT_FAILED,
                    "ORDER_AMOUNT: " + orderAmountInfo.finalAmount()
            );
    }

    @Transactional
    public boolean completeOrder(String orderExternalId) {
        return orderPersistence.updateStatusByExternalId(
                orderExternalId,
                Order.OrderStatus.PAID,
                List.of(Order.OrderStatus.CREATED)
        );
    }

    @Transactional
    public boolean failOrder(String orderExternalId) {
        return orderPersistence.updateStatusByExternalId(
                orderExternalId,
                Order.OrderStatus.FAILED,
                List.of(Order.OrderStatus.CREATED)
        );
    }
}

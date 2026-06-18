package io.playground.orderservice.application.order.usecase;

import io.playground.orderservice.application.eventstream.EventProducerPort;
import io.playground.orderservice.application.eventstream.OrderEvent;
import io.playground.orderservice.application.order.port.OrderItemPersistencePort;
import io.playground.orderservice.application.order.port.OrderPersistencePort;
import io.playground.orderservice.domain.order.Order;
import io.playground.orderservice.domain.order.OrderItem;
import io.playground.orderservice.exception.BusinessDetailException;
import io.playground.orderservice.exception.BusinessErrorCode;
import io.playground.orderservice.exception.BusinessException;
import io.playground.orderservice.infrastructure.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CancellationTxService {
    private final OrderPersistencePort orderPersistence;
    private final OrderItemPersistencePort orderItemPersistence;
    private final EventProducerPort eventProducer;
    private final JsonUtil jsonUtil;

    @Transactional(readOnly = true)
    public void validateToCancelAll(String orderExternalId) {
        // 전체취소 가능한지 확인
        List<OrderItem> orderItems = orderItemPersistence
                .findAllByOrderExternalIdAndOrderStatus(
                        orderExternalId,
                        Order.OrderStatus.PAID
                );

        if (orderItems.isEmpty() || !orderItems.stream()
                .allMatch(oi -> oi.getCanceledQuantity() == 0))
            throw new BusinessDetailException(
                    BusinessErrorCode.ORDER_CANCELLATION_FAILED,
                    "ORDER_NOT_FOUND OR INVALID_ORDER_STATUS"
            );
    }

    @Transactional
    public void cancelAll(String idempotencyKey,
                          String orderExternalId,
                          String reason) {
        // 주문 상태 업데이트
        if (!orderPersistence.updateStatusByExternalId(
                orderExternalId,
                Order.OrderStatus.CANCELED,
                List.of(Order.OrderStatus.PAID)
        ))
            throw new BusinessException(
                    BusinessErrorCode.ORDER_CANCELLATION_FAILED
            );

        // 전체취소 가능 확인 -> 취소 사유 업데이트
        if (!orderItemPersistence.updateCanceledReasonsToCancelAll(
                orderExternalId,
                reason
        ))
            throw new BusinessException(
                    BusinessErrorCode.ORDER_CANCELLATION_FAILED
            );

        // 재고 복구를 위한 이벤트 발행
        eventProducer.produce(
                OrderEvent.EventType.ALL_CANCELED,
                OrderEvent.AllCanceled.builder()
                        .orderExternalId(orderExternalId)
                        .build(),
                idempotencyKey
        );
    }

    @Transactional(readOnly = true)
    public BigDecimal validateAndCalculateAmountToCancelPartially(String orderExternalId,
                                                                  Map<Long, Integer> variantQuantities) {
        // 부분취소 가능한지 확인
        // 예약 실패한 옵션이 있다면, 옵션 ID와 가능 수량을 맵핑하여 예외 반환
        Map<Long, Integer> unavailable = new HashMap<>();
        List<OrderItem> orderItems = orderItemPersistence
                .findAllByVariantIdsAndOrderExternalIdAndOrderStatusIn(
                        orderExternalId,
                        variantQuantities.keySet().stream().toList(),
                        List.of(
                                Order.OrderStatus.PAID,
                                Order.OrderStatus.PARTIAL_CANCELED
                        )
                ).stream()
                .peek(oi -> {
                    if (oi.getQuantity() <
                            oi.getCanceledQuantity() + variantQuantities.get(oi.getVariantId()))
                        unavailable.put(
                                oi.getVariantId(),
                                oi.getQuantity() - oi.getCanceledQuantity()
                        );
                }).toList();

        if (orderItems.isEmpty())
            throw new BusinessDetailException(
                    BusinessErrorCode.ORDER_PARTIAL_CANCELLATION_FAILED,
                    "ORDER_NOT_FOUND OR INVALID_ORDER_STATUS"
            );
        if (!unavailable.isEmpty())
            throw new BusinessDetailException(
                    BusinessErrorCode.ORDER_PARTIAL_CANCELLATION_FAILED,
                    jsonUtil.toJson(unavailable)
            );

        // 취소 금액 계산
        return orderItems.stream()
                .map(oi -> oi.getPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        variantQuantities.get(oi.getVariantId())
                                )
                        )
                ).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void cancelPartially(String idempotencyKey,
                                String orderExternalId,
                                Map<Long, Integer> variantQuantities,
                                String reason,
                                BigDecimal canceledAmount) {
        // 부분취소 가능 확인 -> 취소 수량 += 요청 수량, 취소 사유 업데이트
        if (!orderItemPersistence.updateCanceledInfosToCancelPartially(
                orderExternalId,
                variantQuantities,
                reason
        ))
            throw new BusinessException(
                    BusinessErrorCode.ORDER_PARTIAL_CANCELLATION_FAILED
            );

        // 주문 상태 업데이트
        if (!orderPersistence.updateStatusByExternalId(
                orderExternalId,
                Order.OrderStatus.PARTIAL_CANCELED,
                List.of(
                        Order.OrderStatus.PAID,
                        Order.OrderStatus.PARTIAL_CANCELED
                )
        ))
            throw new BusinessException(
                    BusinessErrorCode.ORDER_PARTIAL_CANCELLATION_FAILED
            );

        // 재고 복구를 위한 이벤트 발행
        eventProducer.produce(
                OrderEvent.EventType.PARTIALLY_CANCELED,
                OrderEvent.PartiallyCanceled.builder()
                        .idempotencyKey(idempotencyKey)
                        .orderExternalId(orderExternalId)
                        .canceledVariantQuantities(variantQuantities)
                        .canceledAmount(canceledAmount)
                        .build(),
                idempotencyKey
        );
    }
}

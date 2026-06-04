package io.playground.orderservice.application.order.usecase;

import io.playground.orderservice.application.eventstream.EventProducerPort;
import io.playground.orderservice.application.eventstream.OrderEvent;
import io.playground.orderservice.application.order.port.OrderItemPersistencePort;
import io.playground.orderservice.application.order.port.OrderPersistencePort;
import io.playground.orderservice.application.saga.dto.ClientDto;
import io.playground.orderservice.application.saga.port.client.PaymentClientPort;
import io.playground.orderservice.domain.order.Order;
import io.playground.orderservice.exception.BusinessDetailException;
import io.playground.orderservice.exception.BusinessErrorCode;
import io.playground.orderservice.exception.BusinessErrorDto;
import io.playground.orderservice.infrastructure.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CancellationService {
    private final OrderPersistencePort orderPersistence;
    private final OrderItemPersistencePort orderItemPersistence;
    private final PaymentClientPort paymentClient;
    private final EventProducerPort eventProducer;
    private final JsonUtil jsonUtil;

    /**
     * 주문 전체취소
     * - 취소 수량 및 사유 업데이트 -> 주문 취소 처리 -> 취소 이벤트 발행 -> 결제 취소 요청
     * - 취소 이벤트로 재고 복구, 사용자 알림 등 후속 처리
     *
     * @param idempotencyKey 멱등성 보장키
     * @param orderExternalId 주문번호
     * @param reason 취소 사유
     */
    @Retryable(
            noRetryFor = BusinessDetailException.class,
            backoff = @Backoff(
                    delay = 1000, multiplier = 2
            )
    )
    @Transactional
    public void cancelAll(String idempotencyKey,
                          String orderExternalId,
                          String paymentKey,
                          String reason) {
        // 취소 수량 = 주문 수량 확인, 취소 사유 업데이트
        // + orderExternalId & orderItems 존재하는지 확인
        if (!orderItemPersistence.updateCanceledReasonsByOrderExternalId(
                orderExternalId,
                reason
        ))
            throw new BusinessDetailException(
                    BusinessErrorCode.ORDER_CANCELLATION_FAILED,
                    "ORDER_NOT_FOUND OR NOT_INITIAL_CANCELLATION"
            );

        // 주문 상태 업데이트
        if (!orderPersistence.updateStatusByExternalId(
                orderExternalId,
                Order.OrderStatus.CANCELED,
                List.of(Order.OrderStatus.PAID)
        ))
            throw new BusinessDetailException(
                    BusinessErrorCode.ORDER_CANCELLATION_FAILED,
                    "ORDER_STATUS_MUST_BE_PAID"
            );

        // 재고 복구를 위한 이벤트 발행
        eventProducer.produce(
                OrderEvent.EventType.CANCEL_ALL,
                OrderEvent.CancelAll.builder()
                        .orderExternalId(orderExternalId)
                        .build(),
                idempotencyKey
        );

        // 전체취소 요청
        try {
            paymentClient.cancelPayment(
                    ClientDto.CancelPaymentRequest.builder()
                            .idempotencyKey(idempotencyKey)
                            .paymentKey(paymentKey)
                            .reason(reason)
                            .build()
            );
        } catch (BusinessDetailException e) {
            throw new BusinessDetailException(
                    BusinessErrorCode.ORDER_CANCELLATION_FAILED,
                    jsonUtil.toJson(BusinessErrorDto.from(e))
            );
        }
    }

    /**
     * 주문 부분취소
     *  - 취소 수량 및 사유 업데이트 -> 주문 부분취소 처리 -> 부분취소 이벤트 발행 -> 결제 부분취소 요청
     *  - 부분취소 이벤트로 재고 복구, 사용자 알림 등 후속 처리
     *
     * @param idempotencyKey 멱등성 보장키
     * @param orderExternalId 주문번호
     * @param variantQuantities {옵션 Id: 취소 수량} 맵
     * @param reason 취소 사유
     */
    @Retryable(
            noRetryFor = BusinessDetailException.class,
            backoff = @Backoff(
                    delay = 1000, multiplier = 2
            )
    )
    @Transactional
    public void cancelPartially(String idempotencyKey,
                                String orderExternalId,
                                String paymentKey,
                                Map<Long, Integer> variantQuantities,
                                String reason) {
        // 취소 수량 += 요청 수량, 취소 사유 업데이트
        // orderItem 존재하는지, 취소 가능한 수량인지 확인
        if (!orderItemPersistence.updateCanceledQuantityAndReasonsByVariantIds(
                variantQuantities,
                reason
        ))
            throw new BusinessDetailException(
                    BusinessErrorCode.ORDER_PARTIAL_CANCELLATION_FAILED,
                    "ORDER_NOT_FOUND OR INVALID_PARTIAL_CANCELLATION_REQUEST"
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
            throw new BusinessDetailException(
                    BusinessErrorCode.ORDER_PARTIAL_CANCELLATION_FAILED,
                    "ORDER_STATUS_MUST_BE_PAID_OR_PARTIAL_CANCELED"
            );

        // 취소 금액 계산
        BigDecimal cancelsAmount = orderItemPersistence
                .findAllByVariantIdsAndOrderExternalId(
                        variantQuantities.keySet().stream().toList(),
                        orderExternalId
                ).stream()
                .map(oi -> oi.getPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        variantQuantities.get(oi.getVariantId())
                                )
                        )
                ).reduce(BigDecimal.ZERO, BigDecimal::add);


        // 재고 복구를 위한 이벤트 발행
        eventProducer.produce(
                OrderEvent.EventType.CANCEL_PARTIALLY,
                OrderEvent.CancelPartially.builder()
                        .idempotencyKey(idempotencyKey)
                        .orderExternalId(orderExternalId)
                        .canceledVariantQuantities(variantQuantities)
                        .canceledAmount(cancelsAmount)
                        .build(),
                idempotencyKey
        );

        // 부분취소 요청
        try {
            paymentClient.cancelPartially(
                    ClientDto.CancelPartiallyRequest.builder()
                            .idempotencyKey(idempotencyKey)
                            .paymentKey(paymentKey)
                            .amount(cancelsAmount)
                            .reason(reason)
                            .build()
            );
        } catch (BusinessDetailException e) {
            throw new BusinessDetailException(
                    BusinessErrorCode.ORDER_PARTIAL_CANCELLATION_FAILED,
                    jsonUtil.toJson(BusinessErrorDto.from(e))
            );
        }
    }
}

package io.playground.orderservice.application.order.usecase;

import io.playground.orderservice.application.saga.dto.ClientDto;
import io.playground.orderservice.application.saga.port.client.PaymentClientPort;
import io.playground.orderservice.exception.BusinessDetailException;
import io.playground.orderservice.exception.BusinessErrorCode;
import io.playground.orderservice.exception.BusinessErrorDto;
import io.playground.orderservice.infrastructure.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

// ToDo: 결제 정상 취소된건지, 주문과 결제의 취소내역 비교 필요
@Service
@RequiredArgsConstructor
public class CancellationService {
    private final PaymentClientPort paymentClient;
    private final CancellationTxService cancellationTxService;
    private final JsonUtil jsonUtil;

    /**
     * 주문 전체취소
     * - 취소 가능여부 확인 -> 결제 취소 요청 -> 취소 수량 및 사유 업데이트 -> 주문 취소 처리 -> 취소 이벤트 발행
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
    public void cancelAll(String idempotencyKey,
                          String orderExternalId,
                          String paymentKey,
                          String reason) {
        // 전체취소 가능한지 확인
        cancellationTxService.validateToCancelAll(orderExternalId);

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

        // 전체취소로 주문 상태 업데이트
        cancellationTxService.cancelAll(
                idempotencyKey,
                orderExternalId,
                reason
        );
    }

    /**
     * 주문 부분취소
     *  - 취소 가능여부 및 금액 확인 -> 결제 취소 요청 -> 취소 수량 및 사유 업데이트 -> 주문 취소 처리 -> 취소 이벤트 발행
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
    public void cancelPartially(String idempotencyKey,
                                String orderExternalId,
                                String paymentKey,
                                Map<Long, Integer> variantQuantities,
                                String reason) {
        // 부분취소 가능한지 확인 -> 취소금액 계산
        BigDecimal cancelsAmount = cancellationTxService
                .validateAndCalculateAmountToCancelPartially(
                        orderExternalId,
                        variantQuantities
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

        // 부분취소로 주문 상태 업데이트
        cancellationTxService.cancelPartially(
                idempotencyKey,
                orderExternalId,
                variantQuantities,
                reason,
                cancelsAmount
        );
    }
}

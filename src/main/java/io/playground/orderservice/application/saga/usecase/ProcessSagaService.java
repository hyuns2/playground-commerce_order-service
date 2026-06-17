package io.playground.orderservice.application.saga.usecase;

import io.playground.orderservice.application.saga.dto.ClientDto;
import io.playground.orderservice.application.saga.port.persistence.ProcessSagaPersistencePort;
import io.playground.orderservice.domain.saga.ProcessSaga;
import io.playground.orderservice.exception.BusinessDetailException;
import io.playground.orderservice.exception.BusinessErrorCode;
import io.playground.orderservice.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProcessSagaService {
    private final ProcessSagaPersistencePort sagaPersistence;
    private final ProcessStepService processStepService;

    /**
     * 주문 생성 사가 프로세스
     *  - 재고 예약 -> 주문 스냅샷 조회 + 주문 생성
     *  - STOCKS_RESERVED -> ORDER_CREATED
     *
     *  + 주문번호 자체로 멱등성을 보장한다.
     *  + 보상 트랜잭션은 수행하지 않는다. (배치로 진행)
     *  + 재고 부족과 같이 재시도가 무의미한 실패를 제외하여 재시도를 한다.
     *
     * @param userId 유저 ID
     * @param orderExternalId 주문번호
     * @param variantQuantities {옵션 ID: 주문 수량} 맵
     */
    @Retryable(
            noRetryFor = BusinessDetailException.class,
            backoff = @Backoff(
                    delay = 1000, multiplier = 2
            )
    )
    public void processOrder(Long userId,
                             String orderExternalId,
                             Map<Long, Integer> variantQuantities) {
        // 기존 사가를 조회하고, 없으면 새로 생성
        ProcessSaga saga = sagaPersistence
             .findByOrderExternalId(orderExternalId)
             .orElseGet(() ->
                     sagaPersistence.saveAndFlush(
                             ProcessSaga.of(
                                     null,
                                     orderExternalId,
                                     null,
                                     ProcessSaga.ProcessSagaStatus.STARTED,
                                     Instant.now().plus(Duration.ofMinutes(5)),
                                     0,
                                     null
                             )
                     )
             );

        // 실패건이면 재시도 불가로 예외 발생
        if (saga.getStatus() ==
                ProcessSaga.ProcessSagaStatus.COMPENSATED)
            throw new BusinessDetailException(
                    BusinessErrorCode.ORDER_PROCESS_FAILED,
                    "ORDER_STATUS: FAILED"
            );

        // STARTED 또는 STOCK_RESERVED 또는 ORDER_CREATED 상태가 아니라면,
        // 이미 주문 존재 판단 하에 멱등성 보장
        if (
                saga.getStatus() !=
                        ProcessSaga.ProcessSagaStatus.STARTED &&
                saga.getStatus() !=
                        ProcessSaga.ProcessSagaStatus.STOCKS_RESERVED &&
                saga.getStatus() !=
                        ProcessSaga.ProcessSagaStatus.ORDER_CREATED
        )
            return;

        // STARTED 상태면 재고 예약
        if (saga.getStatus() ==
                ProcessSaga.ProcessSagaStatus.STARTED)
            processStepService.reserveStocks(
                    saga,
                    orderExternalId,
                    variantQuantities
            );

        // STOCK_RESERVED 상태면 주문 생성 진행
        if (saga.getStatus() ==
                ProcessSaga.ProcessSagaStatus.STOCKS_RESERVED) {
            List<ClientDto.Snapshot> infos =
                    processStepService.getSnapshots(
                            saga,
                            variantQuantities.keySet().stream().toList()
                    );

            processStepService.doOrder(
                    saga,
                    userId,
                    orderExternalId,
                    variantQuantities,
                    infos
            );
        }

        // 최종 상태에 도달하지 못하면 재시도 가능 예외 발생
        if (saga.getStatus() !=
                ProcessSaga.ProcessSagaStatus.ORDER_CREATED)
            throw new BusinessException(
                    BusinessErrorCode.ORDER_PROCESS_FAILED
            );
    }

    /**
     * 결제 승인 사가 프로세스
     *  - 결제 승인 -> 재고 차감 -> 주문 완료 처리
     *  - PAYMENT_APPROVED -> STOCKS_CONFIRMED -> ORDER_COMPLETED
     *
     *  + 보상 트랜잭션은 수행하지 않는다. (배치로 진행)
     *  + 재고 부족과 같이 재시도가 무의미한 실패를 제외하여 재시도를 한다.
     *
     * @param idempotencyKey 멱등성 보장키
     * @param orderExternalId 주문번호
     * @param paymentKey PG사에서 발급한 결제 고유키
     * @param amount 결제 금액
     */
    @Retryable(
            noRetryFor = BusinessDetailException.class,
            backoff = @Backoff(
                    delay = 1000, multiplier = 2
            )
    )
    public void processPayment(String idempotencyKey,
                               String orderExternalId,
                               String paymentKey,
                               BigDecimal amount) {
        // 결제 가능한 상태인지 검증
        processStepService.validatePayment(
                orderExternalId,
                amount
        );

        // 존재하지 않으면, process 과정을 완료하지 않은 것으로 판단
        // -> 재시도 불가로 예외 발생
        ProcessSaga saga = sagaPersistence
                .findByOrderExternalId(orderExternalId)
                .orElseThrow(() ->
                        new BusinessDetailException(
                                BusinessErrorCode.ORDER_PAYMENT_FAILED,
                                "ORDER_PROCESS_FAILED"
                        )
                );

        // 실패건이면 재시도 불가로 예외 발생
        if (saga.getStatus() ==
                ProcessSaga.ProcessSagaStatus.COMPENSATED)
            throw new BusinessDetailException(
                    BusinessErrorCode.ORDER_PAYMENT_FAILED,
                    "ORDER_STATUS: FAILED"
            );

        // STARTED 또는 STOCK_RESERVED 상태면,
        // process 과정을 완료하지 않은 것으로 판단
        // -> 재시도 불가로 예외 발생
        if (
                saga.getStatus() ==
                        ProcessSaga.ProcessSagaStatus.STARTED ||
                saga.getStatus() ==
                        ProcessSaga.ProcessSagaStatus.STOCKS_RESERVED
        )
            throw new BusinessDetailException(
                    BusinessErrorCode.ORDER_PAYMENT_FAILED,
                    "ORDER_PROCESS_FAILED"
            );

        // ORDER_CREATED 상태면, 결제 승인 진행
        if (!sagaPersistence.updatePaymentKey(
                orderExternalId, paymentKey))
            throw new BusinessDetailException(
                    BusinessErrorCode.ORDER_PAYMENT_FAILED,
                    "PAYMENT_UPDATE_FAILED"
            );

        if (saga.getStatus() ==
                ProcessSaga.ProcessSagaStatus.ORDER_CREATED)
            processStepService.approvePayment(
                    saga,
                    idempotencyKey,
                    orderExternalId,
                    paymentKey,
                    amount
            );

        // PAYMENT_COMPLETED 상태면, 재고 차감 확정 진행
        if (saga.getStatus() ==
                ProcessSaga.ProcessSagaStatus.PAYMENT_COMPLETED)
            processStepService.confirmStocks(
                    saga,
                    orderExternalId
            );

        // STOCKS_CONFIRMED 상태면, 주문 완료 처리 진행
        if (saga.getStatus() ==
                ProcessSaga.ProcessSagaStatus.STOCKS_CONFIRMED)
            processStepService.completeOrder(
                    saga,
                    orderExternalId
            );

        // 최종 상태에 도달하지 못하면 재시도 가능 예외 발생
        if (saga.getStatus() !=
                ProcessSaga.ProcessSagaStatus.ORDER_COMPLETED)
            throw new BusinessException(
                    BusinessErrorCode.ORDER_PAYMENT_FAILED
            );
    }
}

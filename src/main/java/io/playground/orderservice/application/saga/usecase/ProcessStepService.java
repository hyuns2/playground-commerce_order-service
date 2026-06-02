package io.playground.orderservice.application.saga.usecase;

import io.playground.orderservice.application.eventstream.EventProducerPort;
import io.playground.orderservice.application.eventstream.OrderEvent;
import io.playground.orderservice.application.order.usecase.OrderService;
import io.playground.orderservice.application.saga.dto.ClientDto;
import io.playground.orderservice.application.saga.port.client.CatalogClientPort;
import io.playground.orderservice.application.saga.port.client.InventoryClientPort;
import io.playground.orderservice.application.saga.port.client.PaymentClientPort;
import io.playground.orderservice.application.saga.port.persistence.ProcessSagaPersistencePort;
import io.playground.orderservice.domain.saga.ProcessSaga;
import io.playground.orderservice.exception.BusinessDetailException;
import io.playground.orderservice.exception.BusinessErrorCode;
import io.playground.orderservice.exception.BusinessErrorDto;
import io.playground.orderservice.infrastructure.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ProcessStepService {
    private final ProcessSagaPersistencePort sagaPersistence;
    private final OrderService orderService;
    private final CatalogClientPort catalogClient;
    private final InventoryClientPort inventoryClient;
    private final PaymentClientPort paymentClient;
    private final EventProducerPort eventProducer;
    private final JsonUtil jsonUtil;

    private void execute(Runnable action,
                         ProcessSaga saga,
                         ProcessSaga.ProcessSagaStatus successStatus,
                         ProcessSaga.ProcessSagaStatus beforeStatus,
                         BusinessErrorCode errorCode) {
        try {
            action.run();

            if (sagaPersistence.updateStatus(
                    saga.getOrderExternalId(),
                    successStatus,
                    beforeStatus
            ))
                saga.updateStatus(successStatus);
        } catch (BusinessDetailException e) {
            if (sagaPersistence.updateStatus(
                    saga.getOrderExternalId(),
                    ProcessSaga.ProcessSagaStatus.FAILED,
                    beforeStatus
            ))
                saga.updateStatus(ProcessSaga.ProcessSagaStatus.FAILED);

            throw new BusinessDetailException(
                    errorCode,
                    jsonUtil.toJson(BusinessErrorDto.from(e))
            );
        }
    }

    private <T> T execute(Supplier<T> action,
                          ProcessSaga saga,
                          ProcessSaga.ProcessSagaStatus successStatus,
                          ProcessSaga.ProcessSagaStatus beforeStatus,
                          BusinessErrorCode errorCode) {
        T result;

        try {
            result = action.get();

            if (sagaPersistence.updateStatus(
                    saga.getOrderExternalId(),
                    successStatus,
                    beforeStatus
            ))
                saga.updateStatus(successStatus);
        } catch (BusinessDetailException e) {
            if (sagaPersistence.updateStatus(
                    saga.getOrderExternalId(),
                    ProcessSaga.ProcessSagaStatus.FAILED,
                    beforeStatus
            ))
                saga.updateStatus(ProcessSaga.ProcessSagaStatus.FAILED);

            throw new BusinessDetailException(
                    errorCode,
                    jsonUtil.toJson(BusinessErrorDto.from(e))
            );
        }

        return result;
    }

    public void reserveStocks(ProcessSaga saga,
                              String orderExternalId,
                              Map<Long, Integer> variantQuantities) {
        execute(
                () ->
                        inventoryClient.reserveStocks(
                                orderExternalId,
                                variantQuantities.entrySet().stream()
                                        .map(entry ->
                                                ClientDto.ReservationRequest.builder()
                                                        .variantId(entry.getKey())
                                                        .quantity(entry.getValue())
                                                        .build()
                                        ).toList()
                        ),
                saga,
                ProcessSaga.ProcessSagaStatus.STOCKS_RESERVED,
                ProcessSaga.ProcessSagaStatus.STARTED,
                BusinessErrorCode.ORDER_PROCESS_FAILED
        );
    }

    public List<ClientDto.Snapshot> getSnapshots(ProcessSaga saga,
                                                 List<Long> variantIds) {
        return execute(
                () ->
                        catalogClient.getSnapshots(
                                variantIds
                        ),
                saga,
                ProcessSaga.ProcessSagaStatus.STOCKS_RESERVED,
                ProcessSaga.ProcessSagaStatus.STOCKS_RESERVED,
                BusinessErrorCode.ORDER_PROCESS_FAILED
        );
    }

    @Transactional
    public void doOrder(ProcessSaga saga,
                        Long userId,
                        String orderExternalId,
                        Map<Long, Integer> variantQuantities,
                        List<ClientDto.Snapshot> snapshots) {
        execute(
                () ->
                        orderService.createOrder(
                                userId,
                                orderExternalId,
                                variantQuantities,
                                snapshots
                        ),
                saga,
                ProcessSaga.ProcessSagaStatus.ORDER_CREATED,
                ProcessSaga.ProcessSagaStatus.STOCKS_RESERVED,
                BusinessErrorCode.ORDER_PROCESS_FAILED
        );
    }

    public void validatePayment(String orderExternalId,
                                BigDecimal amount) {
        orderService.validatePayment(orderExternalId, amount);
    }

    public void approvePayment(ProcessSaga saga,
                               String idempotencyKey,
                               String orderExternalId,
                               String paymentKey,
                               BigDecimal amount) {
        execute(
                () ->
                        paymentClient.approvePayment(
                                ClientDto.ApprovePaymentRequest.builder()
                                        .idempotencyKey(idempotencyKey)
                                        .orderExternalId(orderExternalId)
                                        .paymentKey(paymentKey)
                                        .amount(amount)
                                        .build()
                        ),
                saga,
                ProcessSaga.ProcessSagaStatus.PAYMENT_COMPLETED,
                ProcessSaga.ProcessSagaStatus.ORDER_CREATED,
                BusinessErrorCode.ORDER_PAYMENT_FAILED
        );
    }

    public void confirmStocks(ProcessSaga saga,
                              String orderExternalId) {
        execute(
                () ->
                        inventoryClient.confirmStocks(
                                orderExternalId
                        ),
                saga,
                ProcessSaga.ProcessSagaStatus.STOCKS_CONFIRMED,
                ProcessSaga.ProcessSagaStatus.PAYMENT_COMPLETED,
                BusinessErrorCode.ORDER_PAYMENT_FAILED
        );
    }

    @Transactional
    public void completeOrder(ProcessSaga saga,
                              String orderExternalId) {
        execute(
                () -> {
                    if (!orderService.completeOrder(
                            orderExternalId
                    ))
                        throw new BusinessDetailException(
                                BusinessErrorCode.ORDER_PAYMENT_FAILED,
                                "INVALID_ORDER_STATUS"
                        );
                },
                saga,
                ProcessSaga.ProcessSagaStatus.ORDER_COMPLETED,
                ProcessSaga.ProcessSagaStatus.STOCKS_CONFIRMED,
                BusinessErrorCode.ORDER_PAYMENT_FAILED
        );
    }

    @Transactional
    public void compensate(String idempotencyKey,
                           ProcessSaga saga) {
        if (
                saga.getStatus() ==
                        ProcessSaga.ProcessSagaStatus.ORDER_COMPLETED ||
                saga.getStatus() ==
                        ProcessSaga.ProcessSagaStatus.FAILED
        )
            return;

        switch (saga.getStatus()) {
            case STOCKS_CONFIRMED, PAYMENT_COMPLETED, ORDER_CREATED, STOCKS_RESERVED:
                // 결제 존재할 경우, 취소
                if (saga.getPaymentKey() != null)
                    try {
                        paymentClient.cancelPayment(
                                ClientDto.CancelPaymentRequest.builder()
                                        .idempotencyKey(idempotencyKey)
                                        .paymentKey(saga.getPaymentKey())
                                        .reason("Failed to process order")
                                        .build()
                        );
                    } catch (BusinessDetailException e) {
                        throw new BusinessDetailException(
                                BusinessErrorCode.PAYMENT_SERVICE_FAILED,
                                jsonUtil.toJson(BusinessErrorDto.from(e))
                        );
                    }

                // 주문 존재할 경우, 취소
                orderService.failOrder(saga.getOrderExternalId());
            default:
                // 재고 처리 & 알림
                eventProducer.produce(
                        OrderEvent.EventType.ORDER_EXPIRED,
                        OrderEvent.OrderExpired.builder()
                                .orderExternalId(saga.getOrderExternalId())
                                .build(),
                        idempotencyKey
                );

                // 사가 상태 업데이트
                if (
                        sagaPersistence.updateStatus(
                                saga.getOrderExternalId(),
                                ProcessSaga.ProcessSagaStatus.FAILED,
                                saga.getStatus()
                        )
                )
                    saga.updateStatus(
                            ProcessSaga.ProcessSagaStatus.FAILED);
                else
                    throw new BusinessDetailException(
                            BusinessErrorCode.ORDER_EXPIRED_FAILED,
                            "INVALID_ORDER_STATUS"
                    );
        }
    }
}

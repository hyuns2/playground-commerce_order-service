package io.playground.orderservice.application.saga.port.persistence;

import io.playground.orderservice.domain.saga.ProcessSaga;

import java.util.Optional;

public interface ProcessSagaPersistencePort {
    Optional<ProcessSaga> findByOrderExternalId(String orderExternalId);
    ProcessSaga saveAndFlush(ProcessSaga processSaga);

    boolean updateStatus(String orderExternalId,
                         ProcessSaga.ProcessSagaStatus status,
                         ProcessSaga.ProcessSagaStatus beforeStatus);

    boolean updatePaymentKey(String orderExternalId,
                             String paymentKey);
}

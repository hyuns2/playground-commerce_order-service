package io.playground.orderservice.application.saga.port.persistence;

import io.playground.orderservice.domain.saga.ProcessSaga;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProcessSagaPersistencePort {
    Optional<ProcessSaga> findByOrderExternalId(String orderExternalId);

    ProcessSaga saveAndFlush(ProcessSaga processSaga);

    List<ProcessSaga> findExpiredSagas(int limitSize,
                                       int retryCount);

    boolean updateStatus(Long id,
                         ProcessSaga.ProcessSagaStatus status);

    boolean updateStatus(String orderExternalId,
                         ProcessSaga.ProcessSagaStatus status,
                         ProcessSaga.ProcessSagaStatus beforeStatus);

    boolean updatePaymentKey(String orderExternalId,
                             String paymentKey);

    boolean updateRetryCountAndLockedUntil(Long id,
                                           Instant lockedUntil);
}

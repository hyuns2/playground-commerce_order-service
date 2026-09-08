package io.playground.orderservice.application.saga.port.persistence

import io.playground.orderservice.domain.saga.ProcessSaga
import java.time.Instant
import java.util.Optional

interface ProcessSagaPersistencePort {
    fun findByOrderExternalId(orderExternalId: String): Optional<ProcessSaga>

    fun saveAndFlush(processSaga: ProcessSaga): ProcessSaga

    fun findExpiredSagas(limitSize: Int, retryCount: Int): List<ProcessSaga>

    fun updateStatus(id: Long, status: ProcessSaga.ProcessSagaStatus): Boolean

    fun updateStatus(
        orderExternalId: String,
        status: ProcessSaga.ProcessSagaStatus,
        beforeStatus: ProcessSaga.ProcessSagaStatus,
    ): Boolean

    fun updatePaymentKey(orderExternalId: String, paymentKey: String): Boolean

    fun updateRetryCountAndLockedUntil(id: Long, lockedUntil: Instant?): Boolean
}

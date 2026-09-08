package io.playground.orderservice.infrastructure.persistence.saga

import io.playground.orderservice.application.saga.port.persistence.ProcessSagaPersistencePort
import io.playground.orderservice.domain.saga.ProcessSaga
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Optional

@Component
class ProcessSagaPersistenceAdapter(
    private val processSagaRepository: ProcessSagaJpaRepository,
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) : ProcessSagaPersistencePort {
    override fun findByOrderExternalId(orderExternalId: String): Optional<ProcessSaga> =
        processSagaRepository.findByOrderExternalId(orderExternalId)
            .map(ProcessSagaEntity::toDomain)

    override fun saveAndFlush(processSaga: ProcessSaga): ProcessSaga =
        processSagaRepository.saveAndFlush(ProcessSagaEntity.fromDomain(processSaga)).toDomain()

    override fun findExpiredSagas(limitSize: Int, retryCount: Int): List<ProcessSaga> =
        processSagaRepository.findExpiredSagas(limitSize, retryCount)
            .map(ProcessSagaEntity::toDomain)

    override fun updateStatus(id: Long?, status: ProcessSaga.ProcessSagaStatus): Boolean =
        if (ProcessSaga.isActiveStatus(status)) {
            jdbcTemplate.update(
                "UPDATE process_sagas SET status = :status WHERE id = :id",
                mapOf(
                    "id" to id,
                    "status" to status.name,
                ),
            ) == 1
        } else {
            jdbcTemplate.update(
                "UPDATE process_sagas SET status = :status, active = false WHERE id = :id",
                mapOf(
                    "id" to id,
                    "status" to status.name,
                ),
            ) == 1
        }

    override fun updateStatus(
        orderExternalId: String,
        status: ProcessSaga.ProcessSagaStatus,
        beforeStatus: ProcessSaga.ProcessSagaStatus,
    ): Boolean = if (ProcessSaga.isActiveStatus(status)) {
        jdbcTemplate.update(
            "UPDATE process_sagas SET status = :status WHERE order_external_id = :orderExternalId AND status = :beforeStatus",
            mapOf(
                "orderExternalId" to orderExternalId,
                "status" to status.name,
                "beforeStatus" to beforeStatus.name,
            ),
        ) == 1
    } else {
        jdbcTemplate.update(
            "UPDATE process_sagas SET status = :status, active = false WHERE order_external_id = :orderExternalId AND status = :beforeStatus",
            mapOf(
                "orderExternalId" to orderExternalId,
                "status" to status.name,
                "beforeStatus" to beforeStatus.name,
            ),
        ) == 1
    }

    override fun updatePaymentKey(orderExternalId: String, paymentKey: String): Boolean =
        jdbcTemplate.update(
            "UPDATE process_sagas SET payment_key = :paymentKey WHERE order_external_id = :orderExternalId",
            mapOf(
                "orderExternalId" to orderExternalId,
                "paymentKey" to paymentKey,
            ),
        ) == 1

    override fun updateRetryCountAndLockedUntil(id: Long?, lockedUntil: Instant?): Boolean =
        if (lockedUntil != null) {
            jdbcTemplate.update(
                "UPDATE process_sagas SET retry_count = retry_count + 1, locked_until = :lockedUntil WHERE id = :id",
                mapOf(
                    "id" to id,
                    "lockedUntil" to lockedUntil,
                ),
            ) == 1
        } else {
            jdbcTemplate.update(
                "UPDATE process_sagas SET retry_count = retry_count + 1, locked_until = null WHERE id = :id",
                mapOf("id" to id),
            ) == 1
        }
}

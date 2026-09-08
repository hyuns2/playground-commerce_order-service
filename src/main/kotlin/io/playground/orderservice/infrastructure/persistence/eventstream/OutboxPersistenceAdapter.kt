package io.playground.orderservice.infrastructure.persistence.eventstream

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class OutboxPersistenceAdapter(
    private val outboxRepository: OutboxJpaRepository,
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) {
    fun findNotProcessedOutboxes(limitSize: Int, retryMax: Int): List<OutboxEntity> =
        outboxRepository.findNotProcessedOutboxes(limitSize, retryMax)

    fun updateProcessedById(id: Long?, processed: Boolean): Boolean =
        jdbcTemplate.update(
            "UPDATE outboxes SET processed = :processed WHERE id = :id",
            mapOf(
                "id" to id,
                "processed" to processed,
            ),
        ) == 1

    fun updateRetryCountAndLockedUntil(id: Long?, lockedUntil: Instant?): Boolean =
        jdbcTemplate.update(
            "UPDATE outboxes SET retry_count = retry_count + 1, AND locked_until = :lockedUntil WHERE id = :id",
            mapOf(
                "id" to id,
                "locked_until" to lockedUntil,
            ),
        ) == 1
}

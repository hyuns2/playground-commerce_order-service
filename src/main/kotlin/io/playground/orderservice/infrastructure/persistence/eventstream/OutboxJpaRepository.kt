package io.playground.orderservice.infrastructure.persistence.eventstream

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OutboxJpaRepository : JpaRepository<OutboxEntity, Long> {
    @Query(
        value = """
            SELECT * FROM outboxes
            WHERE processed = false
                AND retry_count <= :retryMax
                AND (locked_until IS NULL OR locked_until < NOW())
            ORDER BY occurred_at ASC
            LIMIT :limitSize
            FOR UPDATE SKIP LOCKED;
        """,
        nativeQuery = true,
    )
    fun findNotProcessedOutboxes(limitSize: Int, retryMax: Int): List<OutboxEntity>
}

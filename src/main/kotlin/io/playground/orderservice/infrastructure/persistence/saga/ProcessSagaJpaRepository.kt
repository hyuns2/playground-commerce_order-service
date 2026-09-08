package io.playground.orderservice.infrastructure.persistence.saga

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface ProcessSagaJpaRepository : JpaRepository<ProcessSagaEntity, Long> {
    fun findByOrderExternalId(orderExternalId: String): Optional<ProcessSagaEntity>

    @Query(
        value = """
            SELECT * FROM process_sagas
            WHERE active = true
                AND expires_at < NOW()
                AND retry_count < :retryMax
                AND (locked_until IS NULL OR locked_until < NOW())
            LIMIT :limitSize
            FOR UPDATE SKIP LOCKED;
        """,
        nativeQuery = true,
    )
    fun findExpiredSagas(retryMax: Int, limitSize: Int): List<ProcessSagaEntity>
}

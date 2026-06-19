package io.playground.orderservice.infrastructure.persistence.eventstream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OutboxJpaRepository extends JpaRepository<OutboxEntity, Long> {
    @Query(value = """
            SELECT * FROM outboxes
            WHERE retry_count <= :retryMax
                AND (locked_until IS NULL OR locked_until < NOW())
                AND processed = false
            ORDER BY occurred_at ASC
            LIMIT :limitSize
            FOR UPDATE SKIP LOCKED;
    """, nativeQuery = true)
    List<OutboxEntity> findNotProcessedOutboxes(int limitSize,
                                                int retryMax);
}

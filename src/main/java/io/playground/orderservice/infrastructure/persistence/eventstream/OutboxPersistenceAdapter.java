package io.playground.orderservice.infrastructure.persistence.eventstream;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class OutboxPersistenceAdapter {
    private final OutboxJpaRepository outboxRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<OutboxEntity> findNotProcessedOutboxes(int limitSize,
                                                       int retryMax) {
        return outboxRepository.findNotProcessedOutboxes(
                limitSize, retryMax
        );
    }

    public boolean updateProcessedById(Long id, boolean processed) {
        return jdbcTemplate.update(
                "UPDATE outboxes SET " +
                        "processed = :processed " +
                    "WHERE id = :id",
                Map.of(
                        "id", id,
                        "processed", processed
                )
        ) == 1;
    }

    public boolean updateRetryCountAndLockedUntil(Long id, Instant lockedUntil) {
        return jdbcTemplate.update(
                "UPDATE outboxes " +
                        "SET retry_count = retry_count + 1, " +
                        "AND locked_until = :lockedUntil " +
                        "WHERE id = :id",
                Map.of(
                        "id", id,
                        "locked_until", lockedUntil
                )
        ) == 1;
    }
}

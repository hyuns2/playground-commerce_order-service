package io.playground.orderservice.infrastructure.persistence.saga;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProcessSagaJpaRepository extends JpaRepository<ProcessSagaEntity, Long> {
    Optional<ProcessSagaEntity> findByOrderExternalId(String orderExternalId);

    @Query(value = """
            SELECT * FROM process_sagas
            WHERE expires_at < NOW()
                AND status not in ('ORDER_COMPLETED', 'COMPENSATED')
                AND retry_count <= :retryMax
                AND (locked_until IS NULL OR locked_until < NOW())
            LIMIT :limitSize
            FOR UPDATE SKIP LOCKED;
    """, nativeQuery = true)
    List<ProcessSagaEntity> findExpiredSagas(int retryMax,
                                             int limitSize);
}

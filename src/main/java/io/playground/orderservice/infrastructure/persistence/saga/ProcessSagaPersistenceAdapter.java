package io.playground.orderservice.infrastructure.persistence.saga;

import io.playground.orderservice.application.saga.port.persistence.ProcessSagaPersistencePort;
import io.playground.orderservice.domain.saga.ProcessSaga;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProcessSagaPersistenceAdapter implements ProcessSagaPersistencePort {
    private final ProcessSagaJpaRepository processSagaRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<ProcessSaga> findByOrderExternalId(String orderExternalId) {
        return processSagaRepository.findByOrderExternalId(orderExternalId)
                .map(ProcessSagaEntity::toDomain);
    }

    @Override
    public ProcessSaga saveAndFlush(ProcessSaga processSaga) {
        return processSagaRepository.saveAndFlush(
                ProcessSagaEntity.fromDomain(processSaga)
        ).toDomain();
    }

    @Override
    public List<ProcessSaga> findExpiredSagas(int limitSize,
                                              int retryMax) {
        return processSagaRepository
                .findExpiredSagas(limitSize, retryMax).stream()
                .map(ProcessSagaEntity::toDomain)
                .toList();
    }

    @Override
    public boolean updateStatus(Long id,
                                ProcessSaga.ProcessSagaStatus status) {
        return ProcessSaga.isActiveStatus(status) ?
                jdbcTemplate.update(
                        "UPDATE process_sagas " +
                                "SET status = :status " +
                                "WHERE id = :id",
                        Map.of(
                                "id", id,
                                "status", status.name()
                        )
                )== 1 :
                jdbcTemplate.update(
                        "UPDATE process_sagas " +
                                "SET status = :status, " +
                                    "is_active = false " +
                                "WHERE id = :id",
                        Map.of(
                                "id", id,
                                "status", status.name()
                        )
                )== 1;
    }

    @Override
    public boolean updateStatus(String orderExternalId,
                                ProcessSaga.ProcessSagaStatus status,
                                ProcessSaga.ProcessSagaStatus beforeStatus) {
        return ProcessSaga.isActiveStatus(status) ?
                jdbcTemplate.update(
                        "UPDATE process_sagas " +
                                "SET status = :status " +
                            "WHERE order_external_id = :orderExternalId " +
                                "AND status = :beforeStatus",
                        Map.of(
                                "orderExternalId", orderExternalId,
                                "status", status.name(),
                                "beforeStatus", beforeStatus.name()
                        )
                ) == 1 :
                jdbcTemplate.update(
                        "UPDATE process_sagas " +
                                "SET status = :status, " +
                                    "is_active = false " +
                            "WHERE order_external_id = :orderExternalId " +
                                "AND status = :beforeStatus",
                        Map.of(
                                "orderExternalId", orderExternalId,
                                "status", status.name(),
                                "beforeStatus", beforeStatus.name()
                        )
                ) == 1;
    }

    @Override
    public boolean updatePaymentKey(String orderExternalId,
                                    String paymentKey) {
        return jdbcTemplate.update(
                "UPDATE process_sagas " +
                        "SET payment_key = :paymentKey " +
                    "WHERE order_external_id = :orderExternalId",
                Map.of(
                        "orderExternalId", orderExternalId,
                        "paymentKey", paymentKey
                )
        ) == 1;
    }

    @Override
    public boolean updateRetryCountAndLockedUntil(Long id, Instant lockedUntil) {
        return jdbcTemplate.update(
                "UPDATE process_sagas " +
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

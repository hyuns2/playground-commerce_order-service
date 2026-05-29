package io.playground.orderservice.infrastructure.persistence.saga;

import io.playground.orderservice.application.saga.port.persistence.ProcessSagaPersistencePort;
import io.playground.orderservice.domain.saga.ProcessSaga;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProcessSagaPersistenceAdapter implements ProcessSagaPersistencePort {
    private final ProcessSagaJpaRepository orderSagaRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<ProcessSaga> findByOrderExternalId(String orderExternalId) {
        return orderSagaRepository.findByOrderExternalId(orderExternalId)
                .map(ProcessSagaEntity::toDomain);
    }

    @Override
    public ProcessSaga saveAndFlush(ProcessSaga processSaga) {
        return orderSagaRepository.saveAndFlush(
                ProcessSagaEntity.fromDomain(processSaga)
        ).toDomain();
    }

    @Override
    public boolean updateStatus(String orderExternalId,
                                ProcessSaga.ProcessSagaStatus status,
                                ProcessSaga.ProcessSagaStatus beforeStatus) {
        return jdbcTemplate.update(
                "UPDATE process_sagas " +
                        "SET status = :status " +
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
}

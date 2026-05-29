package io.playground.orderservice.infrastructure.persistence.saga;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessSagaJpaRepository extends JpaRepository<ProcessSagaEntity, Long> {
    Optional<ProcessSagaEntity> findByOrderExternalId(String orderExternalId);
}

package io.playground.orderservice.infrastructure.persistence.eventstream;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxJpaRepository extends JpaRepository<OutboxEntity, String> {
}

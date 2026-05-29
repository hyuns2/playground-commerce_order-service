package io.playground.orderservice.infrastructure.persistence.saga;

import io.playground.orderservice.domain.saga.ProcessSaga;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "process_sagas",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"orderExternalId", "paymentKey"}
                )
        }
)
public class ProcessSagaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderExternalId;

    @Column
    private String paymentKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessSaga.ProcessSagaStatus status;

    @Column(nullable = false)
    private Instant expiresAt;

    public static ProcessSagaEntity fromDomain(ProcessSaga processSaga) {
        return ProcessSagaEntity.builder()
                .orderExternalId(processSaga.getOrderExternalId())
                .status(processSaga.getStatus())
                .expiresAt(processSaga.getExpiresAt())
                .build();
    }

    public ProcessSaga toDomain() {
        return ProcessSaga.of(
                this.id,
                this.orderExternalId,
                this.paymentKey,
                this.status,
                this.expiresAt
        );
    }
}

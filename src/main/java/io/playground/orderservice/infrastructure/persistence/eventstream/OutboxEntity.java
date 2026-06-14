package io.playground.orderservice.infrastructure.persistence.eventstream;

import io.playground.orderservice.application.eventstream.OrderEvent;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "outboxes"
//        indexes = {
//                @Index(name = "idx_processed", columnList = "processed")
//        }
)
public class OutboxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String eventId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderEvent.EventType eventType;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false)
    private String traceId;

    @Column(nullable = false)
    @Lob
    private String payload;

    @Column(nullable = false)
    private boolean processed;

    @Column(nullable = false)
    private int retryCount;

    @Column
    private Instant lockedUntil;

    public static OutboxEntity of(OrderEvent.EventType eventType,
                                  String traceId,
                                  String payload) {
        return OutboxEntity.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .occurredAt(Instant.now())
                .traceId(traceId)
                .payload(payload)
                .processed(false)
                .retryCount(0)
                .lockedUntil(null)
                .build();
    }

    public void updateLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
}

package io.playground.orderservice.infrastructure.kafka.model;

import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventEnvelope {
    private String eventId;
    private String eventType;
    private String payload;
    private String traceId;
    private String occurredAt;

    public static EventEnvelope from(OutboxEntity event) {
        return new EventEnvelope(
                event.getEventId(),
                event.getEventType().name(),
                event.getPayload(),
                event.getTraceId(),
                event.getOccurredAt().toString()
        );
    }
}

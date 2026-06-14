package io.playground.orderservice.infrastructure.kafka.producer;

import io.playground.orderservice.application.eventstream.EventProducerPort;
import io.playground.orderservice.application.eventstream.OrderEvent;
import io.playground.orderservice.infrastructure.kafka.model.EventEnvelope;
import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxEntity;
import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxPersistenceAdapter;
import io.playground.orderservice.infrastructure.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor
public class OutboxHandler {
    private final OutboxPersistenceAdapter outboxPersistence;
    private final EventProducerPort eventProducer;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonUtil jsonUtil;

    @Transactional
    public List<OutboxEntity> findNotProcessedOutboxes(int limitSize,
                                                       int retryMax) {
        List<OutboxEntity> entities = outboxPersistence
                .findNotProcessedOutboxes(limitSize, retryMax);

        for (OutboxEntity entity : entities)
            entity.updateLockedUntil(
                    Instant.now()
                            .plus(Duration.ofMinutes(1))
            );

        return entities;
    }

    @Transactional
    public void markProcessed(OutboxEntity entity) {
        outboxPersistence.updateProcessedById(
                entity.getId(),
                true
        );
    }

    @Transactional
    public void markRetryOrFail(OutboxEntity entity,
                                int retryMax) {
        if (entity.getRetryCount() < retryMax) {
            outboxPersistence.updateRetryCountAndLockedUntil(
                    entity.getId(),
                    null
            );

            return;
        }

        eventProducer.produce(
                OrderEvent.EventType.OUTBOXING_FAILED,
                OrderEvent.OutboxingFailed.builder()
                        .outboxId(entity.getId())
                        .eventId(entity.getEventId())
                        .eventType(entity.getEventType())
                        .build(),
                UUID.randomUUID().toString()
        );
    }

    public Future<SendResult<String, String>> send(OutboxEntity entity) {
        return kafkaTemplate.send(
                "order.events",
                entity.getEventId(),
                jsonUtil.toJson(
                        EventEnvelope.from(entity)
                )
        );
    }

    public void flush() {
        kafkaTemplate.flush();
    }
}

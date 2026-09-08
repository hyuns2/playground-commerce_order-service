package io.playground.orderservice.infrastructure.kafka.producer

import io.playground.orderservice.application.eventstream.EventProducerPort
import io.playground.orderservice.application.eventstream.OrderEvent
import io.playground.orderservice.infrastructure.kafka.model.EventEnvelope
import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxEntity
import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxPersistenceAdapter
import io.playground.orderservice.infrastructure.util.JsonUtil
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.Future

@Component
class OutboxHandler(
    private val outboxPersistence: OutboxPersistenceAdapter,
    private val eventProducer: EventProducerPort,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val jsonUtil: JsonUtil,
) {
    @Transactional
    fun findNotProcessedOutboxes(limitSize: Int, retryMax: Int): List<OutboxEntity> {
        val entities = outboxPersistence.findNotProcessedOutboxes(limitSize, retryMax)
        entities.forEach { it.updateLockedUntil(Instant.now().plus(Duration.ofMinutes(1))) }
        return entities
    }

    @Transactional
    fun markProcessed(entity: OutboxEntity) {
        outboxPersistence.updateProcessedById(entity.id, true)
    }

    @Transactional
    fun markRetryOrFail(entity: OutboxEntity, retryMax: Int) {
        if (entity.retryCount < retryMax) {
            outboxPersistence.updateRetryCountAndLockedUntil(entity.id, null)
            return
        }

        eventProducer.produce(
            OrderEvent.EventType.OUTBOXING_FAILED,
            OrderEvent.OutboxingFailed.builder()
                .outboxId(entity.id)
                .eventId(entity.eventId)
                .eventType(entity.eventType)
                .build(),
            UUID.randomUUID().toString(),
        )
    }

    fun send(entity: OutboxEntity): Future<SendResult<String, String>> =
        kafkaTemplate.send(
            "order.events",
            entity.eventId,
            jsonUtil.toJson(EventEnvelope.from(entity)),
        )

    fun flush() {
        kafkaTemplate.flush()
    }
}

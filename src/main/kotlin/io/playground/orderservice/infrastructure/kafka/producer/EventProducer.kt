package io.playground.orderservice.infrastructure.kafka.producer

import io.playground.orderservice.application.eventstream.EventProducerPort
import io.playground.orderservice.application.eventstream.OrderEvent
import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxEntity
import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxJpaRepository
import io.playground.orderservice.infrastructure.util.JsonUtil
import org.springframework.stereotype.Component

@Component
class EventProducer(
    private val producedEventRepository: OutboxJpaRepository,
    private val jsonUtil: JsonUtil,
) : EventProducerPort {
    override fun <T> produce(eventType: OrderEvent.EventType, payload: T, traceId: String) {
        producedEventRepository.save(OutboxEntity.of(eventType, traceId, jsonUtil.toJson(payload)))
    }
}

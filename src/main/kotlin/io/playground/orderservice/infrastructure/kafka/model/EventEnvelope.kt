package io.playground.orderservice.infrastructure.kafka.model

import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxEntity

data class EventEnvelope(
    val eventId: String,
    val eventType: String,
    val payload: String,
    val traceId: String,
    val occurredAt: String,
) {
    companion object {
        @JvmStatic
        fun from(event: OutboxEntity): EventEnvelope = EventEnvelope(
            event.eventId,
            event.eventType.name,
            event.payload,
            event.traceId,
            event.occurredAt.toString(),
        )
    }
}

package io.playground.orderservice.application.eventstream

interface EventProducerPort {
    fun <T> produce(eventType: OrderEvent.EventType, payload: T, traceId: String)
}

package io.playground.orderservice.application.eventstream;

public interface EventProducerPort {
    <T> void produce(OrderEvent.EventType eventType,
                     T payload,
                     String traceId);
}

package io.playground.orderservice.infrastructure.kafka.producer;

import io.playground.orderservice.application.eventstream.EventProducerPort;
import io.playground.orderservice.application.eventstream.OrderEvent;
import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxEntity;
import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxJpaRepository;
import io.playground.orderservice.infrastructure.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventProducer implements EventProducerPort {
    private final OutboxJpaRepository producedEventRepository;
    private final JsonUtil jsonUtil;

    @Override
    public <T> void produce(OrderEvent.EventType eventType, T payload, String traceId) {
        handle(eventType, payload, traceId);
    }

    private <T> void handle(OrderEvent.EventType eventType,
                            T payload,
                            String traceId) {
        producedEventRepository.save(
                OutboxEntity.of(
                        eventType,
                        traceId,
                        jsonUtil.toJson(payload)
                )
        );
    }
}

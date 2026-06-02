package io.playground.orderservice.infrastructure.batch.outbox;

import io.playground.orderservice.infrastructure.kafka.producer.OutboxHandler;
import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxProcessor implements ItemProcessor<OutboxEntity, OutboxEntity> {
    private final OutboxHandler outboxHandler;

    @Override
    public OutboxEntity process(OutboxEntity event) {
        outboxHandler.handle(event);

        return event;
    }
}

package io.playground.orderservice.infrastructure.kafka.producer;

import io.playground.orderservice.infrastructure.kafka.model.EventEnvelope;
import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxEntity;
import io.playground.orderservice.infrastructure.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor
public class OutboxHandler {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonUtil jsonUtil;

    public Future<SendResult<String, String>> handle(OutboxEntity event) {
        return kafkaTemplate.send(
                "order.events",
                event.getEventId(),
                jsonUtil.toJson(
                        EventEnvelope.from(event)
                )
        );
    }

    public void flush() {
        kafkaTemplate.flush();
    }
}

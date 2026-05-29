package io.playground.orderservice.infrastructure.kafka.producer;

import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public class OutboxHandler {
    private final KafkaProducer kafkaProducer;

    @Transactional
    public void handle(OutboxEntity event) throws ExecutionException, InterruptedException {
        kafkaProducer.produce(event);
    }
}

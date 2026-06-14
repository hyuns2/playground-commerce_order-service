package io.playground.orderservice.infrastructure.worker.outbox;

import io.playground.orderservice.infrastructure.kafka.producer.OutboxHandler;
import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OutboxWorker {
    private final OutboxHandler outboxHandler;
    private final ThreadPoolTaskExecutor outboxWorkerPool;
    private final OutboxWorkerProperties properties;

    @Scheduled(fixedDelayString = "${tuning.outbox-worker.scheduling-interval}")
    public void processOutbox() {
        Map<OutboxEntity, Future<?>> ackMap = outboxHandler
                .findNotProcessedOutboxes(
                        properties.getLimitSize(),
                        properties.getRetryMax()
                ).stream()
                .collect(Collectors.toMap(
                        e -> e,
                        outboxHandler::send
                ));
        outboxHandler.flush();

        for (Map.Entry<OutboxEntity, Future<?>> entry : ackMap.entrySet()) {
            outboxWorkerPool.execute(() -> {
                try {
                    entry.getValue().get();

                    outboxHandler.markProcessed(entry.getKey());
                } catch (Exception e) {
                    outboxHandler.markRetryOrFail(
                            entry.getKey(),
                            properties.getRetryMax()
                    );
                }
            });
        }
    }
}

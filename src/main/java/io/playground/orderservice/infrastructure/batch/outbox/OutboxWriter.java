package io.playground.orderservice.infrastructure.batch.outbox;

import io.playground.orderservice.infrastructure.kafka.producer.OutboxHandler;
import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxEntity;
import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxJdbcTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OutboxWriter implements ItemWriter<OutboxEntity> {
    private final OutboxHandler outboxHandler;
    private final OutboxJdbcTemplate outboxJdbcTemplate;

    @Override
    public void write(Chunk<? extends OutboxEntity> chunk) {
        Map<OutboxEntity, Future<?>> ackMap = chunk.getItems().stream()
                .collect(Collectors.toMap(
                        e -> e,
                        outboxHandler::handle
                ));
        outboxHandler.flush();

        List<Long> targetIds = new ArrayList<>();
        for (Map.Entry<OutboxEntity, Future<?>> entry : ackMap.entrySet()) {
            try {
                entry.getValue().get();
                targetIds.add(entry.getKey().getId());
            } catch (Exception ignored) {
            }
        }

        outboxJdbcTemplate.updateProcessedByIds(targetIds);
    }
}

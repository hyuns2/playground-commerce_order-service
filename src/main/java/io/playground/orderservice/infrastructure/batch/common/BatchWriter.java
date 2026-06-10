package io.playground.orderservice.infrastructure.batch.common;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BatchWriter implements ItemWriter<Void> {
    @Override
    public void write(Chunk<? extends Void> chunk) {
        return;
    }
}

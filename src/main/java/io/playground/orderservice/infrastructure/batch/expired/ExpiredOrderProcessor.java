package io.playground.orderservice.infrastructure.batch.expired;

import io.playground.orderservice.application.saga.usecase.ProcessSagaService;
import io.playground.orderservice.infrastructure.persistence.saga.ProcessSagaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExpiredOrderProcessor implements ItemProcessor<ProcessSagaEntity, Void> {
    private final ProcessSagaService orderSagaService;

    @Override
    public Void process(ProcessSagaEntity processSaga) {
        orderSagaService.compensate(
                UUID.randomUUID().toString(),
                processSaga.toDomain()
        );

        return null;
    }
}

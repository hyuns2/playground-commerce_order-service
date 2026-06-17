package io.playground.orderservice.infrastructure.worker.compensation;

import io.playground.orderservice.application.saga.usecase.ProcessStepService;
import io.playground.orderservice.domain.saga.ProcessSaga;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CompensationWorker {
    private final ProcessStepService processStepService;
    private final ThreadPoolTaskExecutor compensationWorkerPool;
    private final CompensationWorkerProperties properties;

    @Scheduled(fixedDelayString = "${tuning.compensation-worker.scheduling-interval}")
    public void processCompensation() {
        List<ProcessSaga> expiredSagas =
                processStepService.findExpiredSagas(
                        properties.getLimitSize(),
                        properties.getRetryMax()
                );

        for (ProcessSaga saga : expiredSagas) {
            compensationWorkerPool.execute(() -> {
                try {
                    processStepService.compensate(
                            UUID.randomUUID().toString(),
                            saga
                    );

                    processStepService.markCompensated(saga);
                } catch (Exception e) {
                    processStepService.markRetryOrFail(
                            saga,
                            properties.getRetryMax()
                    );
                }
            });
        }
    }
}

package io.playground.orderservice.infrastructure.worker.config;

import io.playground.orderservice.infrastructure.worker.compensation.CompensationWorkerProperties;
import io.playground.orderservice.infrastructure.worker.outbox.OutboxWorkerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@RequiredArgsConstructor
public class WorkerConfig {
    private final CompensationWorkerProperties compensationWorkerProperties;
    private final OutboxWorkerProperties outboxWorkerProperties;

    @Bean
    public ThreadPoolTaskScheduler taskScheduler(@Value("${tuning.scheduler.pool-size}")
                                                     int poolSize) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setThreadNamePrefix("common-scheduler-");
        scheduler.setPoolSize(poolSize);

        scheduler.initialize();
        return scheduler;
    }

    @Bean
    public ThreadPoolTaskExecutor compensationWorkerPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setThreadNamePrefix("compensation-worker-");
        executor.setCorePoolSize(compensationWorkerProperties.getPoolSize());
        executor.setMaxPoolSize(compensationWorkerProperties.getPoolSize());

        // 작업이 몰릴 때 큐에 쌓지 않고 바로 실행하도록 설정 (Backpressure)
        executor.setQueueCapacity(0);
        executor.setRejectedExecutionHandler(
                new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()
        );

        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor outboxWorkerPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setThreadNamePrefix("outbox-worker-");
        executor.setCorePoolSize(outboxWorkerProperties.getPoolSize());
        executor.setMaxPoolSize(outboxWorkerProperties.getPoolSize());

        // 작업이 몰릴 때 큐에 쌓지 않고 바로 실행하도록 설정 (Backpressure)
        executor.setQueueCapacity(0);
        executor.setRejectedExecutionHandler(
                new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()
        );

        executor.initialize();
        return executor;
    }
}

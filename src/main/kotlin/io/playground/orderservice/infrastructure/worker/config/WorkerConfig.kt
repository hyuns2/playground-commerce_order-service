package io.playground.orderservice.infrastructure.worker.config

import io.playground.orderservice.infrastructure.worker.compensation.CompensationWorkerProperties
import io.playground.orderservice.infrastructure.worker.outbox.OutboxWorkerProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.util.concurrent.ThreadPoolExecutor

@Configuration
class WorkerConfig(
    private val compensationWorkerProperties: CompensationWorkerProperties,
    private val outboxWorkerProperties: OutboxWorkerProperties,
) {
    @Bean
    fun taskScheduler(@Value("\${tuning.scheduler.pool-size}") poolSize: Int): ThreadPoolTaskScheduler {
        val scheduler = ThreadPoolTaskScheduler()
        scheduler.setThreadNamePrefix("common-scheduler-")
        scheduler.poolSize = poolSize
        scheduler.initialize()
        return scheduler
    }

    @Bean
    fun compensationWorkerPool(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.setThreadNamePrefix("compensation-worker-")
        executor.corePoolSize = compensationWorkerProperties.poolSize
        executor.maxPoolSize = compensationWorkerProperties.poolSize
        executor.queueCapacity = 0
        executor.setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        executor.initialize()
        return executor
    }

    @Bean
    fun outboxWorkerPool(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.setThreadNamePrefix("outbox-worker-")
        executor.corePoolSize = outboxWorkerProperties.poolSize
        executor.maxPoolSize = outboxWorkerProperties.poolSize
        executor.queueCapacity = 0
        executor.setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        executor.initialize()
        return executor
    }
}

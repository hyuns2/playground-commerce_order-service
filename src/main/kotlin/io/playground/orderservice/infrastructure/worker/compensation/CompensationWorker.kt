package io.playground.orderservice.infrastructure.worker.compensation

import io.playground.orderservice.application.saga.usecase.ProcessStepService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CompensationWorker(
    private val processStepService: ProcessStepService,
    private val compensationWorkerPool: ThreadPoolTaskExecutor,
    private val properties: CompensationWorkerProperties,
) {
    @Scheduled(fixedDelayString = "\${tuning.compensation-worker.scheduling-interval}")
    fun processCompensation() {
        val expiredSagas = processStepService.findExpiredSagas(
            properties.limitSize,
            properties.retryMax,
        )

        expiredSagas.forEach { saga ->
            compensationWorkerPool.execute {
                try {
                    processStepService.compensate(UUID.randomUUID().toString(), saga)
                    processStepService.markCompensated(saga)
                } catch (_: Exception) {
                    processStepService.markRetryOrFail(saga, properties.retryMax)
                }
            }
        }
    }
}

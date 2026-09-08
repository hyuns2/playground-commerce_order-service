package io.playground.orderservice.infrastructure.worker.outbox

import io.playground.orderservice.infrastructure.kafka.producer.OutboxHandler
import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import java.util.concurrent.Future

@Component
class OutboxWorker(
    private val outboxHandler: OutboxHandler,
    private val outboxWorkerPool: ThreadPoolTaskExecutor,
    private val properties: OutboxWorkerProperties,
) {
    @Scheduled(fixedDelayString = "\${tuning.outbox-worker.scheduling-interval}")
    fun processOutbox() {
        val ackMap: Map<OutboxEntity, Future<*>> = outboxHandler
            .findNotProcessedOutboxes(
                properties.limitSize,
                properties.retryMax,
            ).associateWith(outboxHandler::send)
        outboxHandler.flush()

        ackMap.entries.forEach { entry ->
            outboxWorkerPool.execute {
                try {
                    entry.value.get()
                    outboxHandler.markProcessed(entry.key)
                } catch (_: Exception) {
                    outboxHandler.markRetryOrFail(entry.key, properties.retryMax)
                }
            }
        }
    }
}

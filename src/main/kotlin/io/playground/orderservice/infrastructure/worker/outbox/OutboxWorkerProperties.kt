package io.playground.orderservice.infrastructure.worker.outbox

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "tuning.outbox-worker")
data class OutboxWorkerProperties(
    val schedulingInterval: Long,
    val poolSize: Int,
    val limitSize: Int,
    val retryMax: Int,
)

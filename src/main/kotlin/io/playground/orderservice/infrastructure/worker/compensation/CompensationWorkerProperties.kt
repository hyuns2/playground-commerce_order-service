package io.playground.orderservice.infrastructure.worker.compensation

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "tuning.compensation-worker")
data class CompensationWorkerProperties(
    val schedulingInterval: Long,
    val poolSize: Int,
    val limitSize: Int,
    val retryMax: Int,
)

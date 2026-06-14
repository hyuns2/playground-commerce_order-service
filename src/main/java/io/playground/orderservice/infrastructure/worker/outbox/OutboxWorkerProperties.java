package io.playground.orderservice.infrastructure.worker.outbox;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tuning.outbox-worker")
@Getter
@AllArgsConstructor
public class OutboxWorkerProperties {
    private final long schedulingInterval;
    private final int poolSize;
    private final int limitSize;
    private final int retryMax;
}

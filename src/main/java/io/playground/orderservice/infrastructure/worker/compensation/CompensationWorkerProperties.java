package io.playground.orderservice.infrastructure.worker.compensation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tuning.compensation-worker")
@Getter
@AllArgsConstructor
public class CompensationWorkerProperties {
    private final long schedulingInterval;
    private final int poolSize;
    private final int limitSize;
    private final int retryMax;
}

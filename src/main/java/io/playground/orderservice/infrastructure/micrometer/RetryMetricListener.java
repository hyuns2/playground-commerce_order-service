package io.playground.orderservice.infrastructure.micrometer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

@Configuration
@RequiredArgsConstructor
public class RetryMetricListener implements RetryListener {

    private final MeterRegistry registry;

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        Counter.builder("retry.attempts")
                .tag("exception",
                        throwable.getClass().getSimpleName())
                .register(registry)
                .increment();
    }
}

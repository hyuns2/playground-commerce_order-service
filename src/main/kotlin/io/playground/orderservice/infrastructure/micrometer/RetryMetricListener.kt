package io.playground.orderservice.infrastructure.micrometer

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Configuration
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener

@Configuration
class RetryMetricListener(
    private val registry: MeterRegistry,
) : RetryListener {
    override fun <T : Any?, E : Throwable?> onError(
        context: RetryContext,
        callback: RetryCallback<T, E>,
        throwable: Throwable,
    ) {
        Counter.builder("retry.attempts")
            .tag("exception", throwable::class.java.simpleName)
            .register(registry)
            .increment()
    }
}

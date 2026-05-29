package io.playground.orderservice.domain.saga;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ProcessSaga {
    private Long id;

    private String orderExternalId;

    private String paymentKey;

    private ProcessSagaStatus status;

    private Instant expiresAt;

    public enum ProcessSagaStatus {
        STARTED,
        STOCKS_RESERVED,
        ORDER_CREATED,
        PAYMENT_COMPLETED,
        STOCKS_CONFIRMED,
        ORDER_COMPLETED,
        FAILED
    }

    public static ProcessSaga of(Long id,
                                 String orderExternalId,
                                 String paymentKey,
                                 ProcessSagaStatus status,
                                 Instant expiresAt) {
        return new ProcessSaga(id, orderExternalId, paymentKey, status, expiresAt);
    }

    public void updateStatus(ProcessSagaStatus status) {
        this.status = status;
    }
}

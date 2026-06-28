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

    private boolean active;

    private Instant expiresAt;

    // 재시도용 필드
    private int retryCount;

    // 재시도용 필드
    private Instant lockedUntil;

    public enum ProcessSagaStatus {
        STARTED,
        STOCKS_RESERVED,
        ORDER_CREATED,
        PAYMENT_COMPLETED,
        STOCKS_CONFIRMED,
        ORDER_COMPLETED,
        COMPENSATED
    }

    public static boolean isActiveStatus(ProcessSagaStatus status) {
        return status != ProcessSagaStatus.ORDER_COMPLETED &&
                status != ProcessSagaStatus.COMPENSATED;
    }

    public static ProcessSaga of(Long id,
                                 String orderExternalId,
                                 String paymentKey,
                                 ProcessSagaStatus status,
                                 Instant expiresAt,
                                 int retryCount,
                                 Instant lockedUntil) {
        return new ProcessSaga(
                id,
                orderExternalId,
                paymentKey,
                status,
                isActiveStatus(status),
                expiresAt,
                retryCount,
                lockedUntil
        );
    }

    public void updateStatus(ProcessSagaStatus status) {
        this.status = status;

        if (!isActiveStatus(status))
            this.active = false;
    }

    public void updateLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
}

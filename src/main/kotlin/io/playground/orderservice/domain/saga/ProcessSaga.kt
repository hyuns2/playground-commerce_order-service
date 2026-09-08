package io.playground.orderservice.domain.saga

import java.time.Instant

class ProcessSaga(
    val id: Long?,
    val orderExternalId: String,
    val paymentKey: String?,
    var status: ProcessSagaStatus,
    var active: Boolean,
    val expiresAt: Instant,
    val retryCount: Int,
    var lockedUntil: Instant?,
) {
    enum class ProcessSagaStatus {
        STARTED,
        STOCKS_RESERVED,
        ORDER_CREATED,
        PAYMENT_COMPLETED,
        STOCKS_CONFIRMED,
        ORDER_COMPLETED,
        COMPENSATED,
    }

    fun updateStatus(status: ProcessSagaStatus) {
        this.status = status
        if (!isActiveStatus(status)) {
            this.active = false
        }
    }

    fun updateLockedUntil(lockedUntil: Instant?) {
        this.lockedUntil = lockedUntil
    }

    fun isActive(): Boolean = active

    companion object {
        @JvmStatic
        fun isActiveStatus(status: ProcessSagaStatus): Boolean =
            status != ProcessSagaStatus.ORDER_COMPLETED &&
                status != ProcessSagaStatus.COMPENSATED

        @JvmStatic
        fun of(
            id: Long?,
            orderExternalId: String,
            paymentKey: String?,
            status: ProcessSagaStatus,
            expiresAt: Instant,
            retryCount: Int,
            lockedUntil: Instant?,
        ): ProcessSaga = ProcessSaga(
            id,
            orderExternalId,
            paymentKey,
            status,
            isActiveStatus(status),
            expiresAt,
            retryCount,
            lockedUntil,
        )
    }
}

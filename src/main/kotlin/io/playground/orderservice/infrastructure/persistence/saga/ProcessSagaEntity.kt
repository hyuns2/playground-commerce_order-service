package io.playground.orderservice.infrastructure.persistence.saga

import io.playground.orderservice.domain.saga.ProcessSaga
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "process_sagas",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_orderExternalId_paymentKey", columnNames = ["orderExternalId", "paymentKey"]),
    ],
    indexes = [
        Index(name = "idx_active_expiresAt", columnList = "active, expiresAt"),
    ],
)
class ProcessSagaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var orderExternalId: String = "",
    @Column
    var paymentKey: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ProcessSaga.ProcessSagaStatus = ProcessSaga.ProcessSagaStatus.STARTED,
    @Column(nullable = false)
    var active: Boolean = true,
    @Column(nullable = false)
    var expiresAt: Instant = Instant.EPOCH,
    @Column(nullable = false)
    var retryCount: Int = 0,
    @Column
    var lockedUntil: Instant? = null,
) {
    fun toDomain(): ProcessSaga = ProcessSaga.of(
        id,
        orderExternalId,
        paymentKey,
        status,
        expiresAt,
        retryCount,
        lockedUntil,
    )

    companion object {
        @JvmStatic
        fun fromDomain(processSaga: ProcessSaga): ProcessSagaEntity =
            ProcessSagaEntity(
                orderExternalId = processSaga.orderExternalId,
                status = processSaga.status,
                active = processSaga.active,
                expiresAt = processSaga.expiresAt,
                retryCount = processSaga.retryCount,
                lockedUntil = processSaga.lockedUntil,
            )
    }
}

package io.playground.orderservice.infrastructure.persistence.eventstream

import io.playground.orderservice.application.eventstream.OrderEvent
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Lob
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "outboxes",
    indexes = [
        Index(name = "idx_processed_occurredAt", columnList = "processed, occurredAt"),
    ],
)
class OutboxEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true, nullable = false)
    var eventId: String = "",
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var eventType: OrderEvent.EventType = OrderEvent.EventType.ORDER_EXPIRED,
    @Column(nullable = false)
    var occurredAt: Instant = Instant.EPOCH,
    @Column(nullable = false)
    var traceId: String = "",
    @Column(nullable = false)
    @Lob
    var payload: String = "",
    @Column(nullable = false)
    var processed: Boolean = false,
    @Column(nullable = false)
    var retryCount: Int = 0,
    @Column
    var lockedUntil: Instant? = null,
) {
    fun updateLockedUntil(lockedUntil: Instant?) {
        this.lockedUntil = lockedUntil
    }

    companion object {
        @JvmStatic
        fun of(eventType: OrderEvent.EventType, traceId: String, payload: String): OutboxEntity =
            OutboxEntity(
                eventId = UUID.randomUUID().toString(),
                eventType = eventType,
                occurredAt = Instant.now(),
                traceId = traceId,
                payload = payload,
                processed = false,
                retryCount = 0,
                lockedUntil = null,
            )
    }
}

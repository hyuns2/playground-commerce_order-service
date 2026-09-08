package io.playground.orderservice.application.eventstream

import java.math.BigDecimal
import kotlin.jvm.JvmRecord

class OrderEvent {
    enum class EventType {
        ORDER_EXPIRED,
        ALL_CANCELED,
        PARTIALLY_CANCELED,
        COMPENSATION_FAILED,
        OUTBOXING_FAILED,
    }

    @JvmRecord
    data class OrderExpired(
        val orderExternalId: String,
    ) {
        companion object {
            @JvmStatic
            fun builder(): Builder = Builder()
        }

        class Builder {
            private var orderExternalId: String? = null

            fun orderExternalId(orderExternalId: String): Builder = apply { this.orderExternalId = orderExternalId }

            fun build(): OrderExpired = OrderExpired(
                requireNotNull(orderExternalId) { "orderExternalId is required" },
            )
        }
    }

    @JvmRecord
    data class AllCanceled(
        val orderExternalId: String,
    ) {
        companion object {
            @JvmStatic
            fun builder(): Builder = Builder()
        }

        class Builder {
            private var orderExternalId: String? = null

            fun orderExternalId(orderExternalId: String): Builder = apply { this.orderExternalId = orderExternalId }

            fun build(): AllCanceled = AllCanceled(
                requireNotNull(orderExternalId) { "orderExternalId is required" },
            )
        }
    }

    @JvmRecord
    data class PartiallyCanceled(
        val idempotencyKey: String,
        val orderExternalId: String,
        val canceledVariantQuantities: Map<Long, Int>,
        val canceledAmount: BigDecimal,
    ) {
        companion object {
            @JvmStatic
            fun builder(): Builder = Builder()
        }

        class Builder {
            private var idempotencyKey: String? = null
            private var orderExternalId: String? = null
            private var canceledVariantQuantities: Map<Long, Int>? = null
            private var canceledAmount: BigDecimal? = null

            fun idempotencyKey(idempotencyKey: String): Builder = apply { this.idempotencyKey = idempotencyKey }
            fun orderExternalId(orderExternalId: String): Builder = apply { this.orderExternalId = orderExternalId }
            fun canceledVariantQuantities(canceledVariantQuantities: Map<Long, Int>): Builder = apply { this.canceledVariantQuantities = canceledVariantQuantities }
            fun canceledAmount(canceledAmount: BigDecimal): Builder = apply { this.canceledAmount = canceledAmount }

            fun build(): PartiallyCanceled = PartiallyCanceled(
                requireNotNull(idempotencyKey) { "idempotencyKey is required" },
                requireNotNull(orderExternalId) { "orderExternalId is required" },
                requireNotNull(canceledVariantQuantities) { "canceledVariantQuantities is required" },
                requireNotNull(canceledAmount) { "canceledAmount is required" },
            )
        }
    }

    @JvmRecord
    data class CompensationFailed(
        val processSagaId: Long?,
        val orderExternalId: String,
    ) {
        companion object {
            @JvmStatic
            fun builder(): Builder = Builder()
        }

        class Builder {
            private var processSagaId: Long? = null
            private var orderExternalId: String? = null

            fun processSagaId(processSagaId: Long?): Builder = apply { this.processSagaId = processSagaId }
            fun orderExternalId(orderExternalId: String): Builder = apply { this.orderExternalId = orderExternalId }

            fun build(): CompensationFailed = CompensationFailed(
                processSagaId,
                requireNotNull(orderExternalId) { "orderExternalId is required" },
            )
        }
    }

    @JvmRecord
    data class OutboxingFailed(
        val outboxId: Long?,
        val eventId: String,
        val eventType: EventType,
    ) {
        companion object {
            @JvmStatic
            fun builder(): Builder = Builder()
        }

        class Builder {
            private var outboxId: Long? = null
            private var eventId: String? = null
            private var eventType: EventType? = null

            fun outboxId(outboxId: Long?): Builder = apply { this.outboxId = outboxId }
            fun eventId(eventId: String): Builder = apply { this.eventId = eventId }
            fun eventType(eventType: EventType): Builder = apply { this.eventType = eventType }

            fun build(): OutboxingFailed = OutboxingFailed(
                outboxId,
                requireNotNull(eventId) { "eventId is required" },
                requireNotNull(eventType) { "eventType is required" },
            )
        }
    }
}

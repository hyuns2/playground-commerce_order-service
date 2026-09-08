package io.playground.orderservice.application.saga.dto

import java.math.BigDecimal
import kotlin.jvm.JvmRecord

class ClientDto {
    @JvmRecord
    data class Snapshot(
        val productId: Long,
        val productName: String,
        val variantId: Long,
        val variantName: String,
        val price: BigDecimal,
    )

    @JvmRecord
    data class ReservationRequest(
        val variantId: Long,
        val quantity: Int,
    ) {
        companion object {
            @JvmStatic
            fun builder(): Builder = Builder()
        }

        class Builder {
            private var variantId: Long? = null
            private var quantity: Int? = null

            fun variantId(variantId: Long): Builder = apply { this.variantId = variantId }
            fun quantity(quantity: Int): Builder = apply { this.quantity = quantity }

            fun build(): ReservationRequest = ReservationRequest(
                requireNotNull(variantId) { "variantId is required" },
                requireNotNull(quantity) { "quantity is required" },
            )
        }
    }

    @JvmRecord
    data class ApprovePaymentRequest(
        val idempotencyKey: String,
        val orderExternalId: String,
        val paymentKey: String,
        val amount: BigDecimal,
    ) {
        companion object {
            @JvmStatic
            fun builder(): Builder = Builder()
        }

        class Builder {
            private var idempotencyKey: String? = null
            private var orderExternalId: String? = null
            private var paymentKey: String? = null
            private var amount: BigDecimal? = null

            fun idempotencyKey(idempotencyKey: String): Builder = apply { this.idempotencyKey = idempotencyKey }
            fun orderExternalId(orderExternalId: String): Builder = apply { this.orderExternalId = orderExternalId }
            fun paymentKey(paymentKey: String): Builder = apply { this.paymentKey = paymentKey }
            fun amount(amount: BigDecimal): Builder = apply { this.amount = amount }

            fun build(): ApprovePaymentRequest = ApprovePaymentRequest(
                requireNotNull(idempotencyKey) { "idempotencyKey is required" },
                requireNotNull(orderExternalId) { "orderExternalId is required" },
                requireNotNull(paymentKey) { "paymentKey is required" },
                requireNotNull(amount) { "amount is required" },
            )
        }
    }

    @JvmRecord
    data class CancelPaymentRequest(
        val idempotencyKey: String,
        val paymentKey: String,
        val reason: String,
    ) {
        companion object {
            @JvmStatic
            fun builder(): Builder = Builder()
        }

        class Builder {
            private var idempotencyKey: String? = null
            private var paymentKey: String? = null
            private var reason: String? = null

            fun idempotencyKey(idempotencyKey: String): Builder = apply { this.idempotencyKey = idempotencyKey }
            fun paymentKey(paymentKey: String): Builder = apply { this.paymentKey = paymentKey }
            fun reason(reason: String): Builder = apply { this.reason = reason }

            fun build(): CancelPaymentRequest = CancelPaymentRequest(
                requireNotNull(idempotencyKey) { "idempotencyKey is required" },
                requireNotNull(paymentKey) { "paymentKey is required" },
                requireNotNull(reason) { "reason is required" },
            )
        }
    }

    @JvmRecord
    data class CancelPartiallyRequest(
        val idempotencyKey: String,
        val paymentKey: String,
        val amount: BigDecimal,
        val reason: String,
    ) {
        companion object {
            @JvmStatic
            fun builder(): Builder = Builder()
        }

        class Builder {
            private var idempotencyKey: String? = null
            private var paymentKey: String? = null
            private var amount: BigDecimal? = null
            private var reason: String? = null

            fun idempotencyKey(idempotencyKey: String): Builder = apply { this.idempotencyKey = idempotencyKey }
            fun paymentKey(paymentKey: String): Builder = apply { this.paymentKey = paymentKey }
            fun amount(amount: BigDecimal): Builder = apply { this.amount = amount }
            fun reason(reason: String): Builder = apply { this.reason = reason }

            fun build(): CancelPartiallyRequest = CancelPartiallyRequest(
                requireNotNull(idempotencyKey) { "idempotencyKey is required" },
                requireNotNull(paymentKey) { "paymentKey is required" },
                requireNotNull(amount) { "amount is required" },
                requireNotNull(reason) { "reason is required" },
            )
        }
    }
}

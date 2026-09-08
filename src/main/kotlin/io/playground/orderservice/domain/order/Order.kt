package io.playground.orderservice.domain.order

class Order(
    val id: Long?,
    val externalId: String,
    val userId: Long?,
    var status: OrderStatus,
) {
    enum class OrderStatus {
        CREATED,
        PAID,
        COMPLETED,
        CANCELED,
        PARTIAL_CANCELED,
        FAILED,
    }

    companion object {
        @JvmStatic
        fun of(
            id: Long?,
            externalId: String,
            userId: Long?,
            status: OrderStatus,
        ): Order = Order(id, externalId, userId, status)
    }
}

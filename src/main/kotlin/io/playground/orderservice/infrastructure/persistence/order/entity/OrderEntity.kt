package io.playground.orderservice.infrastructure.persistence.order.entity

import io.playground.orderservice.domain.order.Order
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "orders")
class OrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true, nullable = false)
    var externalId: String = "",
    @Column(nullable = false)
    var userId: Long = 0,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: Order.OrderStatus = Order.OrderStatus.CREATED,
) {
    fun toDomain(): Order = Order.of(id, externalId, userId, status)

    companion object {
        @JvmStatic
        fun fromDomain(order: Order): OrderEntity =
            OrderEntity(
                externalId = order.externalId,
                userId = requireNotNull(order.userId) { "order.userId is required" },
                status = order.status,
            )
    }
}

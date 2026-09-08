package io.playground.orderservice.infrastructure.persistence.order.entity

import io.playground.orderservice.domain.order.OrderItem
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.math.BigDecimal

@Entity
@Table(
    name = "order_items",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_variantId_orderId", columnNames = ["variantId", "orderId"]),
    ],
)
class OrderItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: OrderEntity? = null,
    @Column(nullable = false)
    var variantId: Long = 0,
    @Column(nullable = false)
    var variantName: String = "",
    @Column(nullable = false)
    var productId: Long = 0,
    @Column(nullable = false)
    var productName: String = "",
    @Column(nullable = false)
    var price: BigDecimal = BigDecimal.ZERO,
    @Column(nullable = false)
    var quantity: Int = 0,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderItem.OrderItemStatus = OrderItem.OrderItemStatus.NONE,
    @Column(nullable = false)
    var canceledQuantity: Int = 0,
    @Column
    var canceledReason: String? = null,
) {
    fun toDomain(): OrderItem = OrderItem.of(
        id,
        order?.id,
        variantId,
        variantName,
        productId,
        productName,
        price,
        quantity,
        status,
        canceledQuantity,
        canceledReason,
    )

    companion object {
        @JvmStatic
        fun fromDomain(orderItem: OrderItem, orderEntity: OrderEntity): OrderItemEntity =
            OrderItemEntity(
                order = orderEntity,
                variantId = orderItem.variantId,
                variantName = orderItem.variantName,
                productId = orderItem.productId,
                productName = orderItem.productName,
                price = orderItem.price,
                quantity = orderItem.quantity,
                status = orderItem.status,
                canceledQuantity = orderItem.canceledQuantity,
                canceledReason = orderItem.canceledReason,
            )
    }
}

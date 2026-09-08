package io.playground.orderservice.infrastructure.persistence.order.entity

import io.playground.orderservice.domain.order.OrderAmount
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "order_amounts")
class OrderAmountEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: OrderEntity? = null,
    @Column(nullable = false)
    var totalAmount: BigDecimal = BigDecimal.ZERO,
    @Column(nullable = false)
    var discountAmount: BigDecimal = BigDecimal.ZERO,
    @Column(nullable = false)
    var deliveryFee: BigDecimal = BigDecimal.ZERO,
    @Column(nullable = false)
    var finalAmount: BigDecimal = BigDecimal.ZERO,
) {
    fun toDomain(): OrderAmount = OrderAmount.of(
        id,
        order?.id,
        totalAmount,
        discountAmount,
        deliveryFee,
        finalAmount,
    )

    companion object {
        @JvmStatic
        fun fromDomain(orderAmount: OrderAmount, orderEntity: OrderEntity): OrderAmountEntity =
            OrderAmountEntity(
                order = orderEntity,
                totalAmount = orderAmount.totalAmount,
                discountAmount = orderAmount.discountAmount,
                deliveryFee = orderAmount.deliveryFee,
                finalAmount = orderAmount.finalAmount,
            )
    }
}

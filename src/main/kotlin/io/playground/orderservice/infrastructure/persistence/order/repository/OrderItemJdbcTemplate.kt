package io.playground.orderservice.infrastructure.persistence.order.repository

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.stereotype.Repository

@Repository
class OrderItemJdbcTemplate(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) {
    fun updateCanceledInfosToCancelPartially(
        orderExternalId: String,
        cancelsItemQuantities: Map<Long, Int>,
        reason: String,
    ): IntArray = jdbcTemplate.batchUpdate(
        "UPDATE order_items oi JOIN orders o ON oi.order_id = o.id " +
            "SET oi.canceled_quantity = oi.canceled_quantity + :canceledQuantity, oi.canceled_reason = :reason " +
            "WHERE oi.variant_id = :variantId and o.external_id = :orderExternalId " +
            "and oi.canceled_quantity + :canceledQuantity <= oi.quantity",
        cancelsItemQuantities.entries.map { entry ->
            MapSqlParameterSource()
                .addValue("orderExternalId", orderExternalId)
                .addValue("variantId", entry.key)
                .addValue("canceledQuantity", entry.value)
                .addValue("reason", reason)
        }.toTypedArray<SqlParameterSource>(),
    )
}

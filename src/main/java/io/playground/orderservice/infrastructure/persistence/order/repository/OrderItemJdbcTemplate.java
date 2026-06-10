package io.playground.orderservice.infrastructure.persistence.order.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class OrderItemJdbcTemplate {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public int[] updateCanceledQuantityAndReasonsByOrderExternalIdAndVariantIds(String orderExternalId,
                                                                                Map<Long, Integer> cancelsItemQuantities,
                                                                                String reason) {
        return jdbcTemplate.batchUpdate(
                "UPDATE order_items oi " +
                        "JOIN orders o ON oi.order_id = o.id " +
                        "SET oi.canceled_quantity = oi.canceled_quantity + :canceledQuantity, " +
                            "oi.canceled_reason = :reason " +
                    "WHERE o.external_id = :orderExternalId and " +
                        "oi.variant_id = :variantId and " +
                        "oi.canceled_quantity + :canceledQuantity <= oi.quantity",
                cancelsItemQuantities.entrySet().stream()
                        .map(e -> new MapSqlParameterSource()
                                .addValue("orderExternalId", orderExternalId)
                                .addValue("variantId", e.getKey())
                                .addValue("canceledQuantity", e.getValue())
                                .addValue("reason", reason)
                        ).toArray(SqlParameterSource[]::new)
        );
    }
}

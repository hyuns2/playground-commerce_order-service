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

    public int[] updateCanceledQuantityAndReasonsByVariantIds(Map<Long, Integer> cancelsItemQuantities,
                                                              String reason) {
        return jdbcTemplate.batchUpdate(
                "UPDATE order_items SET " +
                        "canceled_quantity = canceled_quantity + :canceledQuantity, " +
                        "canceled_reason = :reason " +
                    "WHERE variant_id = :variantId and " +
                        "quantity >= canceled_quantity + :canceledQuantity",
                cancelsItemQuantities.entrySet().stream()
                        .map(e -> new MapSqlParameterSource()
                                .addValue("variantId", e.getKey())
                                .addValue("canceledQuantity", e.getValue())
                                .addValue("reason", reason)
                        ).toArray(SqlParameterSource[]::new)
        );
    }
}

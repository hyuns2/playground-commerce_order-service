package io.playground.orderservice.infrastructure.persistence.eventstream;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OutboxJdbcTemplate {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public int[] updateProcessedByIds(List<Long> ids) {
        return jdbcTemplate.batchUpdate(
                "UPDATE outboxes SET " +
                        "processed = true " +
                    "WHERE id = :id AND " +
                        "processed = false",
                ids.stream()
                        .map(i -> new MapSqlParameterSource()
                                .addValue("id", i)
                        ).toArray(SqlParameterSource[]::new)
        );
    }
}

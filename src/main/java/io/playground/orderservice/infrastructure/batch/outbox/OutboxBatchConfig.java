package io.playground.orderservice.infrastructure.batch.outbox;

import io.playground.orderservice.application.eventstream.OrderEvent;
import io.playground.orderservice.infrastructure.persistence.eventstream.OutboxEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class OutboxBatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final DataSource dataSource;
    private final OutboxProcessor processor;

    @Bean
    public Job outboxJob() throws Exception {
        return new JobBuilder("outboxJob", jobRepository)
                .start(outboxStep())
                .build();
    }

    @Bean
    public Step outboxStep() throws Exception {
        return new StepBuilder("outboxStep", jobRepository)
                .<OutboxEntity, OutboxEntity>chunk(1, platformTransactionManager)
                .reader(outboxReader())
                .processor(processor)
                .writer(outboxWriter())
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(3)
                .skip(Exception.class)
                .skipLimit(3)
                .build();
    }

    @Bean
    public JdbcPagingItemReader<OutboxEntity> outboxReader() throws Exception {
        JdbcPagingItemReader<OutboxEntity> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(dataSource);
        reader.setPageSize(100);
        reader.setRowMapper(
                (rs, rowNum) ->
                        OutboxEntity.builder()
                                .id(rs.getLong("id"))
                                .eventId(rs.getString("event_id"))
                                .eventType(
                                        OrderEvent.EventType.valueOf(
                                                rs.getString("event_type")
                                        )
                                ).occurredAt(
                                        rs.getTimestamp("occurred_at")
                                                .toInstant()
                                ).traceId(rs.getString("trace_id"))
                                .payload(rs.getString("payload"))
                                .processed(rs.getBoolean("processed"))
                                .build()
                );

        reader.setQueryProvider(queryProvider());
        reader.afterPropertiesSet();
        return reader;
    }

    private MySqlPagingQueryProvider queryProvider() {
        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();

        queryProvider.setSelectClause("SELECT *");
        queryProvider.setFromClause("FROM outboxes");
        queryProvider.setWhereClause("WHERE processed = false");

        queryProvider.setSortKeys(Map.of("id", Order.ASCENDING));

        return queryProvider;
    }

    @Bean
    public JdbcBatchItemWriter<OutboxEntity> outboxWriter() {
        JdbcBatchItemWriter<OutboxEntity> writer = new JdbcBatchItemWriter<>();

        writer.setDataSource(dataSource);
        writer.setSql(
                "UPDATE outboxes " +
                "SET processed = true " +
                "WHERE id = :id"
        );

        writer.setItemSqlParameterSourceProvider(
                new BeanPropertyItemSqlParameterSourceProvider<>()
        );
        writer.afterPropertiesSet();

        return writer;
    }
}

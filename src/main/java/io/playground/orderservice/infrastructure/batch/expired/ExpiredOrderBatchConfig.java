package io.playground.orderservice.infrastructure.batch.expired;

import io.playground.orderservice.domain.saga.ProcessSaga;
import io.playground.orderservice.exception.BusinessDetailException;
import io.playground.orderservice.infrastructure.batch.common.BatchWriter;
import io.playground.orderservice.infrastructure.persistence.saga.ProcessSagaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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
public class ExpiredOrderBatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final DataSource dataSource;
    private final ExpiredOrderProcessor processor;
    private final BatchWriter batchWriter;

    @Bean
    public Job expiredOrderJob() throws Exception {
        return new JobBuilder("expiredOrderJob", jobRepository)
                .start(expiredOrderStep())
                .build();
    }

    @Bean
    public Step expiredOrderStep() throws Exception {
        return new StepBuilder("expiredOrderStep", jobRepository)
                .<ProcessSagaEntity, Void>chunk(1, platformTransactionManager)
                .reader(expiredOrderReader())
                .processor(processor)
                .writer(batchWriter)
                .faultTolerant()

                .retry(Exception.class)
                .noRetry(BusinessDetailException.class)
                .retryLimit(3)

                .skip(BusinessDetailException.class)
                .skipLimit(Integer.MAX_VALUE)
                .build();
    }

    @Bean
    public JdbcPagingItemReader<ProcessSagaEntity> expiredOrderReader() throws Exception {
        JdbcPagingItemReader<ProcessSagaEntity> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(dataSource);
        reader.setPageSize(100);
        reader.setRowMapper(
                (rs, rowNum) ->
                        ProcessSagaEntity.builder()
                                .id(rs.getLong("id"))
                                .orderExternalId(rs.getString("order_external_id"))
                                .paymentKey(rs.getString("payment_key"))
                                .status(ProcessSaga.ProcessSagaStatus.valueOf(
                                        rs.getString("status")
                                )).expiresAt(rs.getTimestamp("expires_at").toInstant())
                                .build()
        );


        reader.setQueryProvider(queryProvider());
        reader.afterPropertiesSet();
        return reader;
    }

    private MySqlPagingQueryProvider queryProvider() {
        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();

        queryProvider.setSelectClause("SELECT *");
        queryProvider.setFromClause("FROM process_sagas");
        queryProvider.setWhereClause(
                "WHERE status != 'ORDER_COMPLETED' " +
                        "AND status != 'FAILED' " +
                        "AND expires_at < NOW()"
        );

        queryProvider.setSortKeys(Map.of("id", Order.ASCENDING));
        return queryProvider;
    }
}

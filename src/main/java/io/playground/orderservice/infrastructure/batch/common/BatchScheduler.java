package io.playground.orderservice.infrastructure.batch.common;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// ToDo: 배치 스케쥴링 시간 조정 / 청크 사이즈 조정

@Component
@RequiredArgsConstructor
public class BatchScheduler {
    private final JobLauncher jobLauncher;
    private final Job expiredOrderJob;
    private final Job outboxJob;

    @Scheduled(fixedDelay = 15000)
    public void runExpiredOrderJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(expiredOrderJob, params);
    }

    @Scheduled(fixedDelay = 15000)
    public void runOutboxJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(outboxJob, params);
    }
}

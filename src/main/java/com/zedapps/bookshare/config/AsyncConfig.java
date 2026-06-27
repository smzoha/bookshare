package com.zedapps.bookshare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

/**
 * @author smzoha
 * @since 13/2/26
 **/
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("activityPublishExecutor")
    public Executor activityPublishExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("activity-");
        executor.setVirtualThreads(true);
        executor.setConcurrencyLimit(10);

        return executor;
    }
}

package com.zedapps.bookshare.config;

import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutorBuilder()
                .corePoolSize(2)
                .maxPoolSize(5)
                .queueCapacity(1000)
                .threadNamePrefix("activity-")
                .build();

        executor.initialize();
        return executor;
    }
}

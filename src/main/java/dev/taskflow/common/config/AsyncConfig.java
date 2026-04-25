package dev.taskflow.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {


    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(4); //core threads - always alive, waiting for work
        executor.setMaxPoolSize(10); //max threads created when the queue is full
        executor.setQueueCapacity(100); //tasks waiting when all threads are busy
        executor.setThreadNamePrefix("taskflow-async-"); //Prefix for thread names
        executor.setWaitForTasksToCompleteOnShutdown(true); //wait for running tasks to finish on shutdown
        executor.initialize();

        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler(){
        return (throwable, method, params) -> log.error("Async exception in method '{}': {}", method.getName(), throwable.getMessage(), throwable);
    }
}

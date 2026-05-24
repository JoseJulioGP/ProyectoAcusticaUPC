package com.upc.acusticupc.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;

/**
 * Habilita @Async y registra un TaskExecutor basado en virtual threads (Java 25).
 * Spring Boot 4.0.6 también propaga `spring.threads.virtual.enabled=true` a Tomcat,
 * pero el ejecutor de @Async se configura aquí explícitamente para tener nombre.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "ingestionExecutor")
    public TaskExecutor ingestionExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
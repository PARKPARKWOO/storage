package org.woo.storage.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
class ThreadPoolConfig {
    @Bean("grpcThreadPool")
    fun grpcThreadPool(): Executor {
        val executor = ThreadPoolTaskExecutor()
        return executor
    }
}
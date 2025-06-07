package org.woo.storage.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

@Configuration
class ThreadPoolConfig {
    @Bean("grpcThreadPool")
    fun grpcThreadPool(): ThreadPoolTaskExecutor {
        return ThreadPoolTaskExecutor().apply {
            // 기본 설정
            corePoolSize = 5
            maxPoolSize = 20
            queueCapacity = 100
            keepAliveSeconds = 60

            // 스레드 이름 설정 (디버깅/모니터링용)
            setThreadNamePrefix("grpc-")

            // Graceful Shutdown
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(30)

            // 거부 정책 설정
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())

            // 스레드 풀 초기화
            initialize()
        }
    }

    @Bean("fileEventTaskExecutor")
    fun fileEventTaskExecutor(): ThreadPoolTaskExecutor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 3
            maxPoolSize = 10
            queueCapacity = 50
            keepAliveSeconds = 120
            setThreadNamePrefix("file-event-")
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(60)
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
            initialize()
        }
    }

//    @Bean("cpuIntensiveExecutor")
//    fun cpuIntensiveExecutor(): ThreadPoolTaskExecutor {
//        val cpuCount = Runtime.getRuntime().availableProcessors()
//        return ThreadPoolTaskExecutor().apply {
//            corePoolSize = cpuCount
//            maxPoolSize = cpuCount
//            queueCapacity = 50
//            keepAliveSeconds = 60
//
//            setThreadNamePrefix("cpu-intensive-")
//            setWaitForTasksToCompleteOnShutdown(true)
//            setAwaitTerminationSeconds(30)
//            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
//
//            initialize()
//        }
//    }
}
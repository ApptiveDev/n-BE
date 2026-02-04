package masil.backend.global.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);           // 기본 스레드 2개
        executor.setMaxPoolSize(5);            // 최대 스레드 5개
        executor.setQueueCapacity(100);        // 대기 큐 100개
        executor.setThreadNamePrefix("email-");
        executor.initialize();
        return executor;
    }
    
    @Bean(name = "translationTaskExecutor")
    public Executor translationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);           // 기본 스레드 5개
        executor.setMaxPoolSize(10);            // 최대 스레드 10개
        executor.setQueueCapacity(200);        // 대기 큐 200개
        executor.setThreadNamePrefix("translation-");
        executor.initialize();
        return executor;
    }
}

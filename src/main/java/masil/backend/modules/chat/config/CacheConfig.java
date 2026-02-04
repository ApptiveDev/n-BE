package masil.backend.modules.chat.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("translations");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10_000)                    // 최대 10,000개 항목
                .expireAfterWrite(24, TimeUnit.HOURS)   // 24시간 후 만료
                .recordStats());                        // 통계 수집
        return cacheManager;
    }
}

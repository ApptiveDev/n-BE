package masil.backend.modules.chat.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import lombok.extern.slf4j.Slf4j;
import masil.backend.modules.chat.enums.MessageLanguage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TranslationService {
    
    @Value("${google.cloud.translation.api-key:}")
    private String apiKey;
    
    private Translate translate;
    private final Cache<String, String> translationCache;
    
    public TranslationService() {
        // 번역 캐시 초기화
        this.translationCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .recordStats()
                .build();
    }
    
    @PostConstruct
    public void init() {
        try {
            if (apiKey != null && !apiKey.isEmpty()) {
                translate = TranslateOptions.newBuilder()
                        .setApiKey(apiKey)
                        .build()
                        .getService();
                log.info("Google Cloud Translation API 초기화 완료");
            } else {
                log.warn("Google Cloud Translation API 키가 설정되지 않았습니다. 번역 기능이 제한됩니다.");
            }
        } catch (Exception e) {
            log.error("Google Cloud Translation API 초기화 실패", e);
        }
    }
    
    /**
     * 비동기 번역 처리
     * @param content 원문 내용
     * @param sourceLanguage 원본 언어
     * @param targetLanguage 대상 언어
     * @return 번역된 텍스트
     */
    @Async("translationTaskExecutor")
    public CompletableFuture<String> translateAsync(
            String content,
            MessageLanguage sourceLanguage,
            MessageLanguage targetLanguage
    ) {
        String cacheKey = generateCacheKey(content, sourceLanguage, targetLanguage);
        
        // 캐시 확인
        String cached = translationCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("번역 캐시 히트: cacheKey={}", cacheKey);
            return CompletableFuture.completedFuture(cached);
        }
        
        // Google Translation API 호출 (재시도 로직 포함)
        String translatedText = null;
        int maxRetries = 3;
        
        for (int i = 0; i < maxRetries; i++) {
            try {
                if (translate == null) {
                    throw new IllegalStateException("Google Cloud Translation API가 초기화되지 않았습니다.");
                }
                
                Translation translation = translate.translate(
                        content,
                        Translate.TranslateOption.sourceLanguage(sourceLanguage.getCode()),
                        Translate.TranslateOption.targetLanguage(targetLanguage.getCode())
                );
                
                translatedText = translation.getTranslatedText();
                log.debug("번역 완료: source={}, target={}, content={}, translated={}",
                        sourceLanguage.getCode(), targetLanguage.getCode(), content, translatedText);
                break; // 성공 시 루프 종료
                
            } catch (Exception e) {
                log.warn("번역 API 호출 실패 (시도 {}/{}): {}", i + 1, maxRetries, e.getMessage());
                
                if (i == maxRetries - 1) {
                    log.error("번역 실패: 최대 재시도 횟수 초과", e);
                    throw new RuntimeException("번역 실패: " + e.getMessage(), e);
                }
                
                // 지수 백오프 재시도
                try {
                    Thread.sleep((long) Math.pow(2, i) * 100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("번역 재시도 중 인터럽트 발생", ie);
                }
            }
        }
        
        // 캐시 저장
        if (translatedText != null) {
            translationCache.put(cacheKey, translatedText);
        }
        
        return CompletableFuture.completedFuture(translatedText);
    }
    
    /**
     * 캐시 키 생성
     */
    private String generateCacheKey(String content, MessageLanguage sourceLanguage, MessageLanguage targetLanguage) {
        return sourceLanguage.getCode() + ":" + targetLanguage.getCode() + ":" + content;
    }
    
    /**
     * 캐시 통계 조회 (모니터링용)
     */
    public String getCacheStats() {
        return translationCache.stats().toString();
    }
}

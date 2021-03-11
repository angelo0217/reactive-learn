package com.reactive.learn.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j
public class CacheManager<T> {

    @Autowired
    @Qualifier("objectTemplate")
    private ReactiveRedisTemplate<String, T> redisTemplate;

    @Autowired
    private TransactionHelper helper;

    /** 重試機制-使用背壓模式重試1次間隔500毫秒 */
    private Retry retry = Retry.backoff(1, Duration.ofMillis(500));

    public Mono<Boolean> save(String key, T data) {
        String cacheKey = generateKey(key, data.getClass());
        Mono<Boolean> process = redisTemplate.opsForValue().set(cacheKey, data, Duration.ofMinutes(60))
                .retryWhen(retry).onErrorReturn(Boolean.FALSE)
                .log("[CacheManager][save]");
        return helper.doInTransaction(cacheKey, () -> process);
    }

    public Mono<T> get(String key, Class<T> clz) {
        String cacheKey = generateKey(key, clz);
        Mono<T> process = redisTemplate.opsForValue().get(cacheKey)
                .retryWhen(retry).onErrorResume(this::failBack)
                .log("[CacheManager][get]");
        return helper.doInTransaction(cacheKey, () -> process);
    }

    public Mono<Boolean> delete(String key, Class<T> clz) {
        String cacheKey = generateKey(key, clz);
        Mono<Boolean> process = redisTemplate.opsForValue().delete(cacheKey)
                .retryWhen(retry).onErrorReturn(Boolean.FALSE)
                .log("[CacheManager][delete]");
        return helper.doInTransaction(cacheKey, () ->process);
    }

    private Mono<T> failBack(Throwable event) {
        log.error(event.getMessage());
        return Mono.empty();
    }

    private String generateKey(String key, Class<?> clz) {
        return String.format("%s@-@%s", clz.getSimpleName(), key);
    }
}

package com.reactive.learn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class DemoService {
    @Autowired
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisLockService redisLockService;

    /** 重試機制-使用背壓模式重試1次間隔500毫秒 */
    private Retry retry = Retry.backoff(1, Duration.ofMillis(500));

    public Mono<Boolean> put(String key, String word) {
        return redisTemplate.opsForValue().set(key, word);
    }

    public Mono<String> get(String key) {
        return redisTemplate.opsForValue().get(key).map(o -> (String) o);
    }

    public Mono<Boolean> delete(String key) {
        return redisTemplate.opsForValue().delete(key);
    }

    public Flux<String> getList(){
        return redisTemplate.opsForList().range("num", 0, -1).map(n -> "hello-" + n);
    }

    public Mono<Boolean> lock(){
        Mono<Boolean> processer = redisTemplate.opsForValue().set("cacheKey", "data", Duration.ofMinutes(60))
                .retryWhen(retry).onErrorReturn(Boolean.FALSE)
                .log("[CacheManager][save]");

        return redisLockService.doInTransaction("cacheKey", () -> processer);
    }
}

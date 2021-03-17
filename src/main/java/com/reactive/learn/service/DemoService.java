package com.reactive.learn.service;

import com.reactive.learn.config.TransactionHelper;
import com.reactive.learn.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Vector;

@Slf4j
@Service
public class DemoService {
    @Autowired
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TransactionHelper transactionHelper;

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
        Flux<String> flux = redisTemplate.opsForList().range("num", 0, -1).map(n -> "hello-" + n);

        //example flux to list
        List<String> list = flux.collectList().block();
        list.forEach(log::info);
        return flux;
    }

    public Flux<User> getTmpList(){
        List<User> tmp = new Vector<>();
        tmp.add(new User("aaa", 10));
        tmp.add(new User("bbb", 11));
        tmp.add(new User("ccc", 12));
        tmp.add(new User("ddd", 13));

        return Flux.create(sink -> {
            tmp.forEach(user -> sink.next(user));
            sink.complete();
        });
    }

    public Mono<Boolean> lock(){
        Mono<Boolean> processer = redisTemplate.opsForValue().set("cacheKey", "data", Duration.ofMinutes(60))
                .retryWhen(retry).onErrorReturn(Boolean.FALSE)
                .log("[CacheManager][save]");

        return transactionHelper.doInTransaction("cacheKey", () -> processer);
    }
}

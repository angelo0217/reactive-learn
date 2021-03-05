package com.reactive.learn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DemoService {
    @Autowired
    private ReactiveRedisTemplate<String, Object> redisTemplate;

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
}

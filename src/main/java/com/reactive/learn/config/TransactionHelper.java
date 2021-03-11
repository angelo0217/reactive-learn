package com.reactive.learn.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

@Service
public class TransactionHelper {
    @Autowired
    private RedisLockRegistry redisLockRegistry;

    public <T> Mono<T> doInTransaction(String transactionKey, Supplier<Mono<T>> supplier) {
        Lock lock = redisLockRegistry.obtain(transactionKey);
        return Mono.just(0)
                .doFirst(lock::lock)
                .doFinally(dummy ->  lock.unlock())
                .flatMap(dummy -> supplier.get())
//                .subscribeOn(Schedulers.newSingle(transactionKey))
                .log("[Redis Lock]");
    }
}

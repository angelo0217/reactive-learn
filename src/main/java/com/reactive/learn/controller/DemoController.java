package com.reactive.learn.controller;

import com.reactive.learn.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class DemoController {
    @Autowired
    private DemoService demoService;

    @GetMapping("/save/{key}")
    public Mono<Boolean> put(@PathVariable("key") String key) {

        return demoService.put(key, "test");
    }

    @GetMapping("/read/{key}")
    public Mono<String> get(@PathVariable("key") String key) {

        return demoService.get(key);
    }

    @DeleteMapping("/{key}")
    public Mono<Boolean> delete(@PathVariable("key") String key) {

        return demoService.delete(key);
    }

    @GetMapping("/list")
    public Flux<String> getList(){
        return demoService.getList().delayElements(Duration.ofSeconds(1));
    }
}

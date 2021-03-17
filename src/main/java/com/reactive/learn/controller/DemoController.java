package com.reactive.learn.controller;

import com.reactive.learn.config.ws.WsManager;
import com.reactive.learn.model.User;
import com.reactive.learn.model.WsSessionVo;
import com.reactive.learn.service.DemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Function;

@Slf4j
@RestController
public class DemoController {
    @Autowired
    private DemoService demoService;

    @Autowired
    private WsManager wsManager;

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

    @GetMapping(value = "/list", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getList(){
        return demoService.getList().delayElements(Duration.ofSeconds(1));
    }

    @GetMapping(value = "/listtmp", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<User> getListTmp(){
        return demoService.getTmpList().delayElements(Duration.ofSeconds(1));
    }

    @GetMapping("/lock")
    public Mono<Boolean> getLock(){
        return demoService.lock();
    }

    @GetMapping("/send/{id}")
    public Mono<Void> sendWs(@PathVariable("id") String id){
        String test = "hellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohello";
        StringBuffer sb = new StringBuffer();
        for(int i =0 ; i < 1024; i++){
            sb.append(test).append(test).append(test).append(test).append(test).append(test).append(test).append(test).append(test).append(test).append(test);
        }
        test = sb.toString();
        sb = new StringBuffer();
        for(int i =0 ; i < 12; i++){
            sb.append(test);
        }

        log.info("size: {}", sb.toString().length());

        WsSessionVo wsSessionVo = wsManager.get(id);

        wsSessionVo.getSink().next(wsSessionVo.getWebSocketSession().textMessage(sb.toString()));

        StringBuffer finalSb = sb;

        wsSessionVo.getSink().next(wsSessionVo.getWebSocketSession().binaryMessage((new Function<DataBufferFactory, DataBuffer>() {
            @Override
            public DataBuffer apply(DataBufferFactory dataBufferFactory) {
                return dataBufferFactory.wrap(finalSb.toString().getBytes(StandardCharsets.UTF_8));
            }
        })));
        return Mono.empty();
    }
}

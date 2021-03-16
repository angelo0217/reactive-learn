package com.reactive.learn.config.ws;

import com.reactive.learn.model.WsSessionVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
@Slf4j
public class ReactiveWebSocketByteHandler implements WebSocketHandler {
    private Map<String, WsSessionVo> wsSessionVoMap = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Mono<Void> out = session.send(Flux.create(sink->{ this.save(sink, session);})).then();

        Mono<Void> in = session.receive().doOnNext(webSocketMessage -> {
            this.toAll(webSocketMessage.getPayload().asByteBuffer());
        }).doOnCancel(() -> {
            log.info("on cancel");
            wsSessionVoMap.remove(session.getId());
        }).doOnComplete(()->{
            log.info("on complete");
            wsSessionVoMap.remove(session.getId());
        }).then();

        return Mono.zip(in, out).then();
    }

    public void save(FluxSink sink, WebSocketSession webSocketSession){
        log.info("save {}", webSocketSession.getId());
        wsSessionVoMap.put(webSocketSession.getId(), new WsSessionVo(sink, webSocketSession));
    }

    private void toAll(ByteBuffer msg){
        Set<String> keys =  wsSessionVoMap.keySet();
        keys.forEach(key ->{
            WsSessionVo wsSessionVo = wsSessionVoMap.get(key);
            log.info("id: {}, sink: {}", wsSessionVo.getWebSocketSession().getId(), wsSessionVo.getSink().isCancelled());
            if(!wsSessionVo.getSink().isCancelled()){
                wsSessionVo.getSink().next(wsSessionVo.getWebSocketSession().binaryMessage((new Function<DataBufferFactory, org.springframework.core.io.buffer.DataBuffer>() {
                    @Override
                    public DataBuffer apply(DataBufferFactory dataBufferFactory) {
                        return dataBufferFactory.wrap(msg.array());
                    }
                })));
            }
        });
    }
}

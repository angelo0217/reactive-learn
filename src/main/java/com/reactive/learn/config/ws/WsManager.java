package com.reactive.learn.config.ws;

import com.reactive.learn.model.WsSessionVo;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WsManager {
    private Map<String, WsSessionVo> webSocketSessionMap = new ConcurrentHashMap<>();

    public void save(FluxSink sink, WebSocketSession webSocketSession){
        webSocketSessionMap.put(webSocketSession.getId(), new WsSessionVo(sink, webSocketSession));
    }

    public WsSessionVo get(String id){
        return webSocketSessionMap.get(id);
    }
}

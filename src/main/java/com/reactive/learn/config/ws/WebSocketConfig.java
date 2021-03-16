package com.reactive.learn.config.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import org.springframework.web.reactive.socket.server.upgrade.TomcatRequestUpgradeStrategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class WebSocketConfig {
    @Autowired
    private ReactiveWebSocketHandler reactiveWebSocketHandler;

    @Bean
    public HandlerMapping handlerMapping(){
        Map<String, WebSocketHandler> map = new ConcurrentHashMap<>();
        map.put("/ws", reactiveWebSocketHandler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);
        mapping.setOrder(-1);

        return mapping;

    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter(){
//        return new WebSocketHandlerAdapter(webSocketService());
        return new WebSocketHandlerAdapter();
    }

//    @Bean
//    public WebSocketService webSocketService(){
//
//        ReactorNettyRequestUpgradeStrategy strategy = new ReactorNettyRequestUpgradeStrategy();
//        strategy.setHandlePing(true);
//        strategy.setMaxFramePayloadLength(1024 * 1024 * 1024);
//        return new HandshakeWebSocketService(strategy);
//    }
}

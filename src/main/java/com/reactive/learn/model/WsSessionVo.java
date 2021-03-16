package com.reactive.learn.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;

@Data
@AllArgsConstructor
public class WsSessionVo {
    private FluxSink sink;
    private WebSocketSession webSocketSession;
}

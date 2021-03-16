package com.reactive.learn.config.ws;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ReactiveWebSocketHandler implements WebSocketHandler {
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        HandshakeInfo handshakeInfo = session.getHandshakeInfo();
        MultiMap<String> values = new MultiMap<>();
        UrlEncoded.decodeTo(handshakeInfo.getUri().getQuery(), values, "UTF-8");
        String token = values.getString("token");

        log.info("token: {}", token);

        log.info("session: {}", session.getId());

        Mono<Void> in = session.receive().doOnNext(webSocketMessage -> {
            this.executeMessage(webSocketMessage.getPayloadAsText());
        }).doOnCancel(() -> {
            log.info("on cancel");
        }).doOnComplete(()->{
            log.info("on complete");
        }).then();

        return in;
    }

    private void executeMessage(String msg) {
        log.info("msg: {}", msg);
    }
}

package ch.uzh.ifi.hase.soprafs26.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final LiveWebSocketHandler presenceWebSocketHandler;

    public WebSocketConfig(LiveWebSocketHandler presenceWebSocketHandler) {
        this.presenceWebSocketHandler = presenceWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(presenceWebSocketHandler, "/ws/presence")
            .setAllowedOriginPatterns("*");
    }
}
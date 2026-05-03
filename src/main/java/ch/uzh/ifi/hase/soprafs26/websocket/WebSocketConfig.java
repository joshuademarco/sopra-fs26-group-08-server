package ch.uzh.ifi.hase.soprafs26.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final LiveWebSocketHandler presenceWebSocketHandler;
    private final RaidWebSocketHandler raidWebSocketHandler;
    private final CharacterWebSocketHandler characterWebSocketHandler;

    public WebSocketConfig(LiveWebSocketHandler presenceWebSocketHandler, RaidWebSocketHandler raidWebSocketHandler,
            CharacterWebSocketHandler characterWebSocketHandler) {
        this.presenceWebSocketHandler = presenceWebSocketHandler;
        this.raidWebSocketHandler = raidWebSocketHandler;
        this.characterWebSocketHandler = characterWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(presenceWebSocketHandler, "/ws/presence")
                .setAllowedOriginPatterns("*");
        registry.addHandler(raidWebSocketHandler, "/ws/raid")
                .setAllowedOriginPatterns("*");
        registry.addHandler(characterWebSocketHandler, "/ws/character")
                .setAllowedOriginPatterns("*");
    }
}
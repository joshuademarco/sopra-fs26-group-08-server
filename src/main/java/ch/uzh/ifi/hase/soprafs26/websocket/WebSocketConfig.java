package ch.uzh.ifi.hase.soprafs26.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import ch.uzh.ifi.hase.soprafs26.Application;

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
                .setAllowedOrigins(Application.ALLOWED_ORIGINS);
        registry.addHandler(raidWebSocketHandler, "/ws/raid")
                .setAllowedOrigins(Application.ALLOWED_ORIGINS);
        registry.addHandler(characterWebSocketHandler, "/ws/character")
                .setAllowedOrigins(Application.ALLOWED_ORIGINS);
    }
}
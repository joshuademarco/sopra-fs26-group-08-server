package ch.uzh.ifi.hase.soprafs26.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import ch.uzh.ifi.hase.soprafs26.service.CharacterLiveService;
import ch.uzh.ifi.hase.soprafs26.utils.WebSocketTokenResolver;

@Component
public class CharacterWebSocketHandler extends TextWebSocketHandler {

    private final CharacterLiveService characterLiveService;

    public CharacterWebSocketHandler(CharacterLiveService characterLiveService) {
        this.characterLiveService = characterLiveService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = WebSocketTokenResolver.resolveToken(session);
        try {
            characterLiveService.registerSession(session, token);
        } catch (IllegalStateException exception) {
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        characterLiveService.unregisterSession(session);
    }
}

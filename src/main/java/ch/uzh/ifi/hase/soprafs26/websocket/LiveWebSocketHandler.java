package ch.uzh.ifi.hase.soprafs26.websocket;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import ch.uzh.ifi.hase.soprafs26.service.LiveService;

@Component
public class LiveWebSocketHandler extends TextWebSocketHandler {

    private final LiveService liveService;

    public LiveWebSocketHandler(LiveService liveService) {
        this.liveService = liveService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = resolveToken(session);
        try {
            liveService.registerSession(session, token);
        } catch (IllegalStateException exception) {
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        liveService.unregisterSession(session);
    }

    private String resolveToken(WebSocketSession session) {
        String cookieHeader = session.getHandshakeHeaders().getFirst(HttpHeaders.COOKIE);
        if (cookieHeader != null && !cookieHeader.isBlank()) {
            for (String cookie : cookieHeader.split(";")) {
                String trimmed = cookie.trim();
                if (trimmed.startsWith("token=")) {
                    return URLDecoder.decode(trimmed.substring("token=".length()), StandardCharsets.UTF_8);
                }
            }
        }

        return null;
    }
}
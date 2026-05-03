package ch.uzh.ifi.hase.soprafs26.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import ch.uzh.ifi.hase.soprafs26.service.RaidLiveService;

@Component
public class RaidWebSocketHandler extends TextWebSocketHandler {

    private final RaidLiveService raidLiveService;

    public RaidWebSocketHandler(RaidLiveService raidLiveService) {
        this.raidLiveService = raidLiveService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        raidLiveService.registerSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        raidLiveService.unregisterSession(session);
    }
}

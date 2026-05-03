package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
public class CharacterLiveService {

    private final UserRepository userRepository;
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public CharacterLiveService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public synchronized void registerSession(WebSocketSession session, String token) throws IOException {
        if (token == null || token.isBlank()) {
            session.close(CloseStatus.POLICY_VIOLATION);
            throw new IllegalStateException("Authentication required");
        }

        User user = userRepository.findByToken(token);
        if (user == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            throw new IllegalStateException("Invalid token");
        }

        session.getAttributes().put("userId", user.getId());
        sessions.put(user.getId(), session);

        // Send initial character snapshot if available
        if (user.getCharacter() != null) {
            sendSnapshot(session, user.getCharacter());
        }
    }

    public synchronized void unregisterSession(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.remove(userId);
        }
    }

    public synchronized void broadcastCharacterUpdate(Long userId, Character character) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(buildPayload(character)));
            } catch (IOException ignored) {
            }
        }
    }

    private void sendSnapshot(WebSocketSession session, Character character) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(buildPayload(character)));
        }
    }

    private String buildPayload(Character character) {
        return new JSONObject()
                .put("type", "CHARACTER_UPDATE")
                .put("level", character.getLevel())
                .put("health", character.getHealth())
                .put("maxHealth", character.getMaxHealth())
                .put("experience", character.getExperience())
                .put("strength", character.getStrength())
                .put("intelligence", character.getIntelligence())
                .put("resilience", character.getResilience())
                .put("characterType", character.getType())
                .toString();
    }
}

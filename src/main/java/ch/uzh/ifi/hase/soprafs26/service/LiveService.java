package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
public class LiveService {

    private final UserRepository userRepository;
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUserIds = new ConcurrentHashMap<>();

    public LiveService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public synchronized void registerSession(WebSocketSession session, String token) throws IOException {
        activeSessions.put(session.getId(), session);

        if (token == null || token.isBlank()) {
            sendSnapshot(session);
            return;
        }

        User user = userRepository.findByToken(token);
        if (user == null) {
            activeSessions.remove(session.getId());
            throw new IllegalStateException("Invalid token");
        }

        sessionUserIds.put(session.getId(), user.getId());

        if (!user.isOnline()) {
            user.setOnline(true);
            userRepository.saveAndFlush(user);
        }

        sendSnapshot(session);
        broadcastSnapshot();
    }

    public synchronized void unregisterSession(WebSocketSession session) {
        activeSessions.remove(session.getId());

        Long userId = sessionUserIds.remove(session.getId());
        if (userId == null) {
            return;
        }

        if (!sessionUserIds.containsValue(userId)) {
            setUserOffline(userId);
            broadcastSnapshot();
        }
    }

    public synchronized void disconnectUser(Long userId) {
        for (Map.Entry<String, Long> entry : sessionUserIds.entrySet()) {
            if (!entry.getValue().equals(userId)) {
                continue;
            }

            WebSocketSession session = activeSessions.get(entry.getKey());
            if (session != null && session.isOpen()) {
                try {
                    session.close(CloseStatus.NORMAL);
                } catch (IOException ignored) {
                    // Ignore close errors; we still clean up server-side state.
                }
            }

            activeSessions.remove(entry.getKey());
        }

        sessionUserIds.entrySet().removeIf(entry -> entry.getValue().equals(userId));
        setUserOffline(userId);
        broadcastSnapshot();
    }

    public synchronized void sendSnapshot(WebSocketSession session) throws IOException {
        if (session == null || !session.isOpen()) {
            return;
        }

        session.sendMessage(new TextMessage(buildSnapshotPayload()));
    }

    public synchronized void broadcastSnapshot() {
        String payload = buildSnapshotPayload();

        for (WebSocketSession session : activeSessions.values()) {
            if (session == null || !session.isOpen()) {
                continue;
            }

            try {
                session.sendMessage(new TextMessage(payload));
            } catch (IOException exception) {
                
            }
        }
    }

    private void setUserOffline(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.isOnline()) {
                user.setOnline(false);
                userRepository.saveAndFlush(user);
            }
        });
    }

    private String buildSnapshotPayload() {
        JSONArray payload = new JSONArray();

        for (User user : userRepository.findAll()) {
            if (!user.isOnline()) {
                continue;
            }

            payload.put(new JSONObject()
                .put("id", user.getId())
                .put("username", user.getUsername())
                .put("status", user.getStatus()));
        }

        return payload.toString();
    }
}
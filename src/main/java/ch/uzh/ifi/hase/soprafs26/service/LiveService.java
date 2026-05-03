package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;

@Service
public class LiveService {

    private static final int PING_INTERVAL_MS = 15_000;
    private static final int SESSION_TIMEOUT_SECONDS = 35;

    private final UserRepository userRepository;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Instant> sessionLastSeen = new ConcurrentHashMap<>();

    public LiveService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public synchronized void registerSession(WebSocketSession session, String token) throws IOException {
        sessions.put(session.getId(), session);
        sessionLastSeen.put(session.getId(), Instant.now());

        if (token == null || token.isBlank()) {
            sendSnapshot(session);
            return;
        }

        User user = userRepository.findByToken(token);
        if (user == null) {
            sessions.remove(session.getId());
            sessionLastSeen.remove(session.getId());
            throw new IllegalStateException("Invalid token");
        }

        Long userId = user.getId();
        session.getAttributes().put("userId", userId);

        if (user.getStatus() != UserStatus.ONLINE) {
            user.setStatus(UserStatus.ONLINE);
            userRepository.saveAndFlush(user);
        }

        sendSnapshot(session);
        broadcastSnapshot();
    }

    public synchronized void unregisterSession(WebSocketSession session) {
        sessions.remove(session.getId());
        sessionLastSeen.remove(session.getId());

        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null && !hasActiveSessionForUser(userId)) {
            setUserOffline(userId);
            broadcastSnapshot();
        }
    }

    public synchronized void disconnectUser(Long userId) {
        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            WebSocketSession session = entry.getValue();
            Long sessionUserId = (Long) session.getAttributes().get("userId");
            if (sessionUserId == null || !sessionUserId.equals(userId)) {
                continue;
            }

            sessions.remove(entry.getKey());
            if (session.isOpen()) {
                try {
                    session.close(CloseStatus.NORMAL);
                } catch (IOException ignored) {
                    // Ignore close errors; we still clean up server-side state.
                }
            }
        }

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

        for (WebSocketSession session : sessions.values()) {
            if (session == null || !session.isOpen()) {
                continue;
            }

            try {
                session.sendMessage(new TextMessage(payload));
            } catch (IOException exception) {

            }
        }
    }

    public void handlePong(WebSocketSession session) {
        sessionLastSeen.put(session.getId(), Instant.now());
    }

    @Scheduled(fixedDelay = PING_INTERVAL_MS)
    public void pingAndSweep() {
        Instant cutoff = Instant.now().minusSeconds(SESSION_TIMEOUT_SECONDS);
        List<WebSocketSession> stale = new ArrayList<>();

        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            WebSocketSession session = entry.getValue();
            if (!session.isOpen()) {
                stale.add(session);
                continue;
            }
            Instant lastSeen = sessionLastSeen.getOrDefault(entry.getKey(), Instant.EPOCH);
            if (!lastSeen.isAfter(cutoff)) {
                stale.add(session);
            } else {
                try {
                    session.sendMessage(new PingMessage());
                } catch (IOException e) {
                    stale.add(session);
                }
            }
        }

        for (WebSocketSession s : stale) {
            unregisterSession(s);
        }
    }

    private void setUserOffline(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getStatus() != UserStatus.OFFLINE) {
                user.setStatus(UserStatus.OFFLINE);
                userRepository.saveAndFlush(user);
            }
        });
    }

    private boolean hasActiveSessionForUser(Long userId) {
        for (WebSocketSession session : sessions.values()) {
            Long sessionUserId = (Long) session.getAttributes().get("userId");
            if (userId.equals(sessionUserId)) {
                return true;
            }
        }

        return false;
    }

    private String buildSnapshotPayload() {
        JSONArray payload = new JSONArray();

        for (User user : userRepository.findAll()) {
            if (user.getStatus() != UserStatus.ONLINE) {
                continue;
            }

            String characterType = user.getCharacter() != null ? user.getCharacter().getType() : null;
            payload.put(new JSONObject()
                    .put("id", user.getId())
                    .put("username", user.getUsername())
                    .put("status", user.getStatus())
                    .put("characterType", characterType));
        }

        return payload.toString();
    }
}
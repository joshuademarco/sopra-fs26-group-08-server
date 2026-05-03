package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.rest.dto.RaidMemberDTO;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
public class RaidLiveService {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void registerSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    public void unregisterSession(WebSocketSession session) {
        sessions.remove(session.getId());
    }

    public synchronized void broadcastRaidUpdate(Long raidId, Long groupId, Integer health, Integer maxHealth,
            String status, List<RaidMemberDTO> members) {
        JSONArray membersJson = new JSONArray();
        if (members != null) {
            for (RaidMemberDTO m : members) {
                membersJson.put(new JSONObject()
                        .put("userId", m.getUserId())
                        .put("health", m.getHealth())
                        .put("maxHealth", m.getMaxHealth()));
            }
        }

        String payload = new JSONObject()
                .put("type", "RAID_UPDATE")
                .put("raidId", raidId)
                .put("groupId", groupId)
                .put("health", health)
                .put("maxHealth", maxHealth)
                .put("status", status)
                .put("members", membersJson)
                .toString();

        for (WebSocketSession session : sessions.values()) {
            if (session == null || !session.isOpen()) {
                continue;
            }
            try {
                session.sendMessage(new TextMessage(payload));
            } catch (IOException ignored) {
            }
        }
    }
}

package ch.uzh.ifi.hase.soprafs26.utils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.WebSocketSession;

public class WebSocketTokenResolver {
    /**
     * Extracts the authentication token from WebSocket handshake cookies.
     * 
     * @param session the WebSocket session
     * @return the token value if present, null otherwise
     */
    public static String resolveToken(WebSocketSession session) {
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

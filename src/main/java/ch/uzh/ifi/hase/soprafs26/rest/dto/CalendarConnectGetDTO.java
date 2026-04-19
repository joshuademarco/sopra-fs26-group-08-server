package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class CalendarConnectGetDTO {
    private String authUrl;
    private boolean connected;

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}

package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class FreeSlotRequestDTO {
    private List<Long> userIds;
    private String from;
    private String to;

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}

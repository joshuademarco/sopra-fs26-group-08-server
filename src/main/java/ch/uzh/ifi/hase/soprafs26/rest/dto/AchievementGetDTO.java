package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.Instant;
import ch.uzh.ifi.hase.soprafs26.constant.AchievementKey;

public class AchievementGetDTO {

    private Long id;
    private AchievementKey key;
    private String name;
    private String description;
    private String icon;
    private Instant earnedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AchievementKey getKey() {
        return key;
    }

    public void setKey(AchievementKey key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Instant getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(Instant earnedAt) {
        this.earnedAt = earnedAt;
    }
}

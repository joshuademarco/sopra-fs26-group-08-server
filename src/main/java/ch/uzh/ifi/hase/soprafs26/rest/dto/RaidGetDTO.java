package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.Instant;

import ch.uzh.ifi.hase.soprafs26.constant.RaidStatus;

public class RaidGetDTO {
    private Long id;
    private String name;
    private RaidStatus status;
    private Instant scheduledTime;
    private Integer health;
    private Integer maxHealth;
    private Integer durationSeconds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RaidStatus getStatus() {
        return status;
    }

    public void setStatus(RaidStatus status) {
        this.status = status;
    }

    public Instant getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Instant scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(Integer health) {
        this.health = health;
    }

    public Integer getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(Integer maxHealth) {
        this.maxHealth = maxHealth;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
}

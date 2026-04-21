package ch.uzh.ifi.hase.soprafs26.entity;

import java.time.Instant;

import ch.uzh.ifi.hase.soprafs26.constant.RaidStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "bossraids")
public class BossRaid {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(nullable = false)
    private String name;

    private Instant scheduledTime;

    @Column(nullable = false)
    private Integer durationSeconds;

    @Column(nullable = false)
    private Integer health;

    @Column(nullable = false)
    private Integer maxHealth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RaidStatus status;

    private Instant startedAt;
    private Instant endedAt;

    public void startRaid() {
        startedAt = Instant.now();
        setStatus(RaidStatus.ACTIVE);
    }

    public void endRaid(RaidStatus outcome) {
        endedAt = Instant.now();
        setStatus(outcome);
    }

    public void applyDamage(int amount) {
        setHealth(Math.max(0, getHealth() - amount));
        if (checkCompletion()) {
            endRaid(RaidStatus.DEFEATED);
        }
    }

    public boolean checkCompletion() {
        return getHealth() == 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Instant scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
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

    public RaidStatus getStatus() {
        return status;
    }

    public void setStatus(RaidStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }
}

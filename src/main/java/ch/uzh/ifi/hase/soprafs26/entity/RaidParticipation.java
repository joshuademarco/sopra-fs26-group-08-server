package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "raid_participations")
public class RaidParticipation {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boss_raid_id", nullable = false)
    private BossRaid bossRaid;

    @Column(nullable = false)
    private Instant joinedAt;

    @Column(nullable = false)
    private Integer damageDealt = 0;

    @Column(nullable = false)
    private Integer tasksCompleted = 0;

    @Column(nullable = false)
    private Integer tasksFailed = 0;

    @PrePersist
    protected void onCreate() {
        this.joinedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BossRaid getBossRaid() {
        return bossRaid;
    }

    public void setBossRaid(BossRaid bossRaid) {
        this.bossRaid = bossRaid;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Integer getDamageDealt() {
        return damageDealt;
    }

    public void setDamageDealt(Integer damageDealt) {
        this.damageDealt = damageDealt;
    }

    public Integer getTasksCompleted() {
        return tasksCompleted;
    }

    public void setTasksCompleted(Integer tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }

    public Integer getTasksFailed() {
        return tasksFailed;
    }

    public void setTasksFailed(Integer tasksFailed) {
        this.tasksFailed = tasksFailed;
    }
}
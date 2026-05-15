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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dropped_item_id")
    private Item droppedItem;

    @Column(nullable = false)
    private Instant joinedAt;

    @Column(nullable = false)
    private Integer damageDealt = 0;

    @Column(nullable = false)
    private Integer tasksCompleted = 0;

    @Column(nullable = false)
    private Integer tasksFailed = 0;

    @Column(nullable = false)
    private Integer xpEarned = 0;

    @Column(nullable = false)
    private Boolean mvp = false;

    private Boolean accepted;

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

    public Item getDroppedItem() {
        return droppedItem;
    }

    public void setDroppedItem(Item droppedItem) {
        this.droppedItem = droppedItem;
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

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public Integer getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(Integer xpEarned) {
        this.xpEarned = xpEarned;
    }

    public Boolean getMvp() {
        return mvp;
    }

    public void setMvp(Boolean mvp) {
        this.mvp = mvp;
    }
}
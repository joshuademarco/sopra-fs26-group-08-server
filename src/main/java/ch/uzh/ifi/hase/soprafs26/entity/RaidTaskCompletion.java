package ch.uzh.ifi.hase.soprafs26.entity;

import java.time.Instant;

import jakarta.persistence.*;

@Entity
@Table(name = "raid_task_completions")
public class RaidTaskCompletion {
    
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_id", nullable = false)
    private RaidParticipation participation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raid_task_id", nullable = false)
    private RaidTask raidTask;

    @Column(nullable = false)
    private Boolean success;

    private Instant completedAt;

    public RaidParticipation getParticipation() {
        return participation;
    }

    public void setParticipation(RaidParticipation participation) {
        this.participation = participation;
    }

    public RaidTask getRaidTask() {
        return raidTask;
    }

    public void setRaidTask(RaidTask raidTask) {
        this.raidTask = raidTask;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

}

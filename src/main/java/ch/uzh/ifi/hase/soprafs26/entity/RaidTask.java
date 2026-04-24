package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

@Entity
public class RaidTask extends Task {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boss_raid_id", nullable = false)
    private BossRaid raid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    private Integer timeLimitSeconds;
    private Integer taskOrder;
    private Integer successfulDamage;
    private Integer groupDamage;
    private Integer requiredCount;

    public BossRaid getRaid() {
        return raid;
    }

    public void setRaid(BossRaid raid) {
        this.raid = raid;
    }

    public User getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(User assignedUser) {
        this.assignedUser = assignedUser;
    }

    public Integer getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(Integer timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public Integer getTaskOrder() {
        return taskOrder;
    }

    public void setTaskOrder(Integer taskOrder) {
        this.taskOrder = taskOrder;
    }

    public Integer getSuccessfulDamage() {
        return successfulDamage;
    }

    public void setSuccessfulDamage(Integer successfulDamage) {
        this.successfulDamage = successfulDamage;
    }

    public Integer getGroupDamage() {
        return groupDamage;
    }

    public void setGroupDamage(Integer groupDamage) {
        this.groupDamage = groupDamage;
    }

    public Integer getRequiredCount() {
        return requiredCount;
    }

    public void setRequiredCount(Integer requiredCount) {
        this.requiredCount = requiredCount;
    }
}

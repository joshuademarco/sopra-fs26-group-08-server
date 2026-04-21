package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

@Entity
public class RaidTask extends Task {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boss_raid_id", nullable = false)
    private BossRaid raid;

    private String timeLimit;
    private Integer successfulDamage;
    private Integer groupDamageOnFailure;
    private Integer requiredCount;

    public BossRaid getRaid() {
        return raid;
    }

    public void setRaid(BossRaid raid) {
        this.raid = raid;
    }

    public String getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(String timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Integer getSuccessfulDamage() {
        return successfulDamage;
    }

    public void setSuccessfulDamage(Integer successfulDamage) {
        this.successfulDamage = successfulDamage;
    }

    public Integer getGroupDamageOnFailure() {
        return groupDamageOnFailure;
    }

    public void setGroupDamageOnFailure(Integer groupDamageOnFailure) {
        this.groupDamageOnFailure = groupDamageOnFailure;
    }

    public Integer getRequiredCount() {
        return requiredCount;
    }

    public void setRequiredCount(Integer requiredCount) {
        this.requiredCount = requiredCount;
    }
}

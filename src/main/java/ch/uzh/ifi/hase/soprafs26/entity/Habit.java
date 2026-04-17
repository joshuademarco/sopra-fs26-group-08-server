package ch.uzh.ifi.hase.soprafs26.entity;

import java.time.Instant;

import ch.uzh.ifi.hase.soprafs26.constant.HabitFrequency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "habits")
public class Habit extends Task {

    // user can have multiple habits
    @ManyToOne(fetch = FetchType.LAZY) // load the related entity only when accessed
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Boolean positive = true;

    // daily, weekly or monthly
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HabitFrequency frequency;

    @Column(nullable = false)
    private Integer streak = 0;

    private Instant dueAt;

    private Instant lastCompletedAt;

    // getters and setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getPositive() {
        return positive;
    }

    public void setPositive(Boolean positive) {
        this.positive = positive;
    }

    public HabitFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(HabitFrequency frequency) {
        this.frequency = frequency;
    }

    public Integer getStreak() {
        return streak;
    }

    public void setStreak(Integer streak) {
        this.streak = streak;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public void setDueAt(Instant dueAt) {
        this.dueAt = dueAt;
    }

    public Instant getLastCompletedAt() {
        return lastCompletedAt;
    }

    public void setLastCompletedAt(Instant lastCompletedAt) {
        this.lastCompletedAt = lastCompletedAt;
    }
}

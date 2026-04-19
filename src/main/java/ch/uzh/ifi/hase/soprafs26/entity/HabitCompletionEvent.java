package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "habit_completion_events")
public class HabitCompletionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //track which habit was completed
    @ManyToOne(fetch = FetchType.LAZY) //each habit can be completed multiple times
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;

    //track which user completed the habit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant completedAt;

    //xp before weather multiplier (just based on habit difficulty)
    @Column(nullable = false)
    private Integer baseXp;

    @Column(nullable = false)
    private Double weatherMultiplier = 1.0;

    //final xp after multiplier: multipliedXp = baseXp * weatherMultiplier
    @Column(nullable = false)
    private Integer multipliedXp;

    //current weather code at time of completion -> for debugging
    private Integer weatherCode;

    @PrePersist
    protected void onCreate() {
        if (this.completedAt == null) {
            this.completedAt = Instant.now();
        }
    }

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Habit getHabit() {
        return habit;
    }

    public void setHabit(Habit habit) {
        this.habit = habit;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getBaseXp() {
        return baseXp;
    }

    public void setBaseXp(Integer baseXp) {
        this.baseXp = baseXp;
    }

    public Double getWeatherMultiplier() {
        return weatherMultiplier;
    }

    public void setWeatherMultiplier(Double weatherMultiplier) {
        this.weatherMultiplier = weatherMultiplier;
    }

    public Integer getMultipliedXp() {
        return multipliedXp;
    }

    public void setMultipliedXp(Integer multipliedXp) {
        this.multipliedXp = multipliedXp;
    }

    public Integer getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(Integer weatherCode) {
        this.weatherCode = weatherCode;
    }
}

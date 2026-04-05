package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "habits")
public class Habit {
    @Id
    @GeneratedValue
    private Long id;

    private String userId; 
    
    private String name; 
    private String resetSchedule; 
    private Integer streak = 0;

    
    public void logCompletion() {
        this.streak += 1;
    }

    public String getProgress() {
        return "0%"; 
    }

    public Float calculateXP() {
        return 420.0f; 
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    //getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getResetSchedule() { return resetSchedule; }
    public void setResetSchedule(String resetSchedule) { this.resetSchedule = resetSchedule; }

    public Integer getStreak() { return streak; }
    public void setStreak(Integer streak) { this.streak = streak; }
}


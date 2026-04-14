package ch.uzh.ifi.hase.soprafs26.entity;


import jakarta.persistence.*;
import java.time.Instant;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;


@MappedSuperclass 
public abstract class Task {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String title;
    
    //physical, cognitive or emotional
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HabitCategory category;     
    
    @Column(nullable = false)
    private Boolean completed = false;

    private Instant completedAt; //null until completed

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist //works with @MappedSuperclass to set createdAt for all subclasses
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    //mark task as done
    public void complete() {
        this.completed = true;
        this.completedAt = Instant.now();
    }

    //getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public HabitCategory getCategory() { return category; }
    public void setCategory(HabitCategory category) { this.category = category; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
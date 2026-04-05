package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.util.Date;


@MappedSuperclass 
public abstract class Task {

    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private String description;
    
    private String category; //physical, cognitive, emotional
    private String type;     
    
    private Float weight;
    private Boolean completed = false;
    private Date completedAt;
    private Date createdAt = new Date();

    //basic methods for now from diagram
    public void complete() {
        this.completed = true;
        this.completedAt = new Date();
    }

    public Float calculateXP() {
        return 0.0f; 
    }

    //getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Float getWeight() { return weight; }
    public void setWeight(Float weight) { this.weight = weight; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
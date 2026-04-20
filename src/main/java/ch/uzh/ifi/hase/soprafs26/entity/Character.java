package ch.uzh.ifi.hase.soprafs26.entity;

import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "characters")
public class Character implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Integer level = 1;

    @Column(nullable = false)
    private Integer health = 10;

    @Column(nullable = false)
    private Integer maxHealth = 10;

    @Column(nullable = false)
    private Integer experience = 0;

    @Column(nullable = false)
    private Integer strength = 1;

    @Column(nullable = false)
    private Integer resilience = 1;

    @Column(nullable = false)
    private Integer intelligence = 1;

    @Column
    private String skinColor;

    @Column
    private String type;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public Character() {
    }

    @PrePersist
    protected void onCreate() {
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addExperience(Integer amount) {
        this.experience += amount;
        if (this.experience >= 100) {
            this.level += 1;
            this.experience -= 100;
            this.maxHealth += 5;
            this.health = this.maxHealth; 
        }
    }

    public void hit(Integer damage) {
        int actualDamage = Math.max(0, damage - this.resilience);
        this.health = Math.max(0, this.health - actualDamage);
    }

    public void heal(Integer amount) {
        this.health = Math.min(this.maxHealth, this.health + amount);
    }

    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public Integer getLevel() { 
        return level; 
    }

    public void setLevel(Integer level) { 
        this.level = level; 
    }

    public Integer getHealth() { 
        return health; 
    }

    public void setHealth(Integer health) { 
        this.health = health; 
    }

    public Integer getMaxHealth() { 
        return maxHealth; 
    }

    public void setMaxHealth(Integer maxHealth) { 
        this.maxHealth = maxHealth; 
    }

    public Integer getExperience() { 
        return experience; 
    }

    public void setExperience(Integer experience) { 
        this.experience = experience; 
    }

    public Integer getStrength() { 
        return strength; 
    }
    public void setStrength(Integer strength) { 
        this.strength = strength; 
    }

    public Integer getResilience() { 
        return resilience; 
    
    }
    public void setResilience(Integer resilience) { 
        this.resilience = resilience; 
    }

    public Integer getIntelligence() { 
        return intelligence; 
    }

    public void setIntelligence(Integer intelligence) { 
        this.intelligence = intelligence; 
    }

    public String getSkinColor() { 
        return skinColor; 
    }

    public void setSkinColor(String skinColor) { 
        this.skinColor = skinColor; 
    }

    public String getType() { 
        return type; 
    }

    public void setType(String type) { 
        this.type = type; 
    }

    public Instant getUpdatedAt() { 
        return updatedAt; 
    }
    public void setUpdatedAt(Instant updatedAt) { 
        this.updatedAt = updatedAt; 
    }

    public User getUser() { 
        return user; 
    
    }
    public void setUser(User user) { 
        this.user = user; 
    }
}
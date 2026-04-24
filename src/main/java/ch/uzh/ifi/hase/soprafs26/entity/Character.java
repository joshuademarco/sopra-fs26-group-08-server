package ch.uzh.ifi.hase.soprafs26.entity;

import java.io.Serializable;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;

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

    @JsonIgnore
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
        // use while loop to handle multiple level-ups
        while (this.experience >= getXpThreshold()) {
            this.experience -= getXpThreshold(); // leftover XP after level up
            this.level += 1;
            this.maxHealth += 5;
            this.health = this.maxHealth; // restore health on level up
        }
    }

    // simple XP progression -> 100XP for level 1, 200XP for level 2 etc.
    public Integer getXpThreshold() {
        return 100 * this.level;
    }

    // update stats based on habit category
    public void increaseStat(HabitCategory category) {
        switch (category) {
            case PHYSICAL:
                this.strength += 1;
                break;
            case COGNITIVE:
                this.intelligence += 1;
                break;
            case EMOTIONAL:
                this.resilience += 1;
                break;
        }
    }

    public void applyNegativeHabitPenalty(Integer weight) {
        int damage;
        if (weight == null || weight < 1) damage = 1;  // default easy
        else if (weight == 1) damage = 1;  // easy
        else if (weight == 2) damage = 2;  // medium
        else damage = 3;  // hard
        
        this.health = Math.max(0, this.health - damage);
        // if health drops to 0 -> completly reset character 
        if (this.health <= 0) {
            resetCharacter();
        }
    }

    private void resetCharacter() {
        this.level = 1;
        this.maxHealth = 10;
        this.health = this.maxHealth;
        this.experience = 0;
        this.strength = 1;
        this.intelligence = 1;
        this.resilience = 1;
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
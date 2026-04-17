package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "characters")
public class Character implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY) // one character per user
    @JoinColumn(name = "user_id", nullable = false, unique = true) // add user_id column to characters table
    private User user;

    @Column(nullable = false)
    private Integer level = 1;

    // health based on habit success and boss raids
    @Column(nullable = false)
    private Integer health = 100;

    // max health increases when character levels up
    @Column(nullable = false)
    private Integer maxHealth = 100;

    @Column(nullable = false)
    private Integer xp = 0;

    // should increase with each level
    // TODO: level up logic
    @Column(nullable = false)
    private Integer xpToNextLevel = 100;

    @Column(nullable = false)
    private Integer strength = 1;

    @Column(nullable = false)
    private Integer intelligence = 1;

    @Column(nullable = false)
    private Integer resilience = 1;

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public Integer getXp() {
        return xp;
    }

    public void setXp(Integer xp) {
        this.xp = xp;
    }

    public Integer getXpToNextLevel() {
        return xpToNextLevel;
    }

    public void setXpToNextLevel(Integer xpToNextLevel) {
        this.xpToNextLevel = xpToNextLevel;
    }

    public Integer getStrength() {
        return strength;
    }

    public void setStrength(Integer strength) {
        this.strength = strength;
    }

    public Integer getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(Integer intelligence) {
        this.intelligence = intelligence;
    }

    public Integer getResilience() {
        return resilience;
    }

    public void setResilience(Integer resilience) {
        this.resilience = resilience;
    }
}

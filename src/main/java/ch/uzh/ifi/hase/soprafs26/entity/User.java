package ch.uzh.ifi.hase.soprafs26.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how
 * the user is stored in the database.
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false)
    private boolean online;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Character character;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Habit> habits = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { 
        return id; }

    public void setId(Long id) { 
        this.id = id; 
    }

    public String getUsername() { 
        return username; 
    }

    public void setUsername(String username) { 
        this.username = username; 
    }

    public String getEmail() { 
        return email; 
    }

    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getPassword() { 
        return password; 
    }

    public void setPassword(String password) { 
        this.password = password; 
    }

    public String getToken() { 
        return token; 
    }

    public void setToken(String token) { 
        this.token = token; 
    }

    public Instant getCreatedAt() { 
        return createdAt; 
    }

    public void setCreatedAt(Instant createdAt) { 
        this.createdAt = createdAt; 
    }

    public Instant getUpdatedAt() { 
        return updatedAt; 
    }

    public void setUpdatedAt(Instant updatedAt) { 
        this.updatedAt = updatedAt; 
    }

    public UserStatus getStatus() { 
        return status; 
    }

    public void setStatus(UserStatus status) {
        this.status = status;
        this.online = status == UserStatus.ONLINE;
    }

    public boolean isOnline() { 
        return online; 
    }

    public void setOnline(boolean online) {
        this.online = online;
        this.status = online ? UserStatus.ONLINE : UserStatus.OFFLINE;
    }

    public Character getCharacter() { 
        return character; 
    
    }
    public void setCharacter(Character character) { 
        this.character = character; 
    }

    public Group getGroup() { 
        return group; 
    }

    public void setGroup(Group group) { 
        this.group = group; 
    }

    public List<Habit> getHabits() { 
        return habits; 
    }

    public void setHabits(List<Habit> habits) { 
        this.habits = habits; 
    }
}
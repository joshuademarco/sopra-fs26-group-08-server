package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "todos")
public class Todo extends Task {

    @ManyToOne(fetch = FetchType.LAZY) //one user can have multiple todos
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Instant dueAt;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public void setDueAt(Instant dueAt) {
        this.dueAt = dueAt;
    }
}

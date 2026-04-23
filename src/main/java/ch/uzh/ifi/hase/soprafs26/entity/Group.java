package ch.uzh.ifi.hase.soprafs26.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "groups")
public class Group implements Serializable {

  @Id
  @GeneratedValue
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private String password;

  @Column
  private String createdBy;

  @Column
  private LocalDateTime createdAt;

  @ManyToMany(mappedBy = "groups", fetch = FetchType.EAGER)
  private Set<User> users = new HashSet<>();

  public Long getId() { 
    return id; 
}

  public void setId(Long id) { 
    this.id = id; 
}

  public String getName() { 
    return name; 
}

  public void setName(String name) { 
    this.name = name; 
}

  public String getPassword() { 
    return password; 
  }

  public void setPassword(String password) { 
    this.password = password; 
  }
  
  public String getCreatedBy() { 
    return createdBy; 
  }

  public void setCreatedBy(String createdBy) { 
    this.createdBy = createdBy; 
  }

  public LocalDateTime getCreatedAt() { 
    return createdAt; 
  }

  public void setCreatedAt(LocalDateTime createdAt) { 
    this.createdAt = createdAt; 
  }

  public Set<User> getUsers() { 
    return users; 
  }

  public void setUsers(Set<User> users) { 
    this.users = users;
  }

  public void addUser(User user) {
    this.users.add(user);
    if (!user.getGroups().contains(this)) {
      user.getGroups().add(this);
    }
  }

  public void removeUser(User user) {
    this.users.remove(user);
    user.getGroups().remove(this);
  }
}
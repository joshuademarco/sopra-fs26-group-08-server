package ch.uzh.ifi.hase.soprafs26.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "GROUPS")
public class Group implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "groups_seq")
  @SequenceGenerator(name = "groups_seq", sequenceName = "groups_seq", allocationSize = 1)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column
  private String description;

  @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
  private List<User> members = new ArrayList<>();

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

  public String getDescription() { 
    return description; 
}

  public void setDescription(String description) { 
    this.description = description; 
}

  public List<User> getMembers() { 
    return members; 
}

  public void setMembers(List<User> members) { 
    this.members = members; 
}
}
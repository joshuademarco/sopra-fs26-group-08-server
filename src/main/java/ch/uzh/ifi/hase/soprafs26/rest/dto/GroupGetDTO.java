package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDateTime;
import java.util.List;

public class GroupGetDTO {

  private Long id;
  private String name;
  private String createdBy;
  private LocalDateTime createdAt;
  private List<GroupMember> users;

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

  public List<GroupMember> getUsers() {
    return users;
  }

  public void setUsers(List<GroupMember> users) {
    this.users = users;
  }

  // inner class for group members -> needed for habit progress
  public static class GroupMember {
    private Long id;
    private String username;
    private String status;
    private Integer level;
    private int completedHabits;
    private int totalHabits;

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public Integer getLevel() {
      return level;
    }

    public void setLevel(Integer level) {
      this.level = level;
    }

    public int getCompletedHabits() {
      return completedHabits;
    }

    public void setCompletedHabits(int completedHabits) {
      this.completedHabits = completedHabits;
    }

    public int getTotalHabits() {
      return totalHabits;
    }

    public void setTotalHabits(int totalHabits) {
      this.totalHabits = totalHabits;
    }
  }
}

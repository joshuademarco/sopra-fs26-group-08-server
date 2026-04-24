package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class RaidTaskDTO {
    private Long id;
    private String title;
    private String description;
    private Integer successfulDamage;
    private Integer groupDamage;
    private Integer timeLimitSeconds;
    private Integer taskOrder;
    private Integer windowStartSeconds;
    private List<Long> completedByUserIds;
    private List<Long> successfullyCompletedByUsers;
    private Long assignedUserId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(Long assignedUserId) {
        this.assignedUserId = assignedUserId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSuccessfulDamage() {
        return successfulDamage;
    }

    public void setSuccessfulDamage(Integer successfulDamage) {
        this.successfulDamage = successfulDamage;
    }

    public Integer getGroupDamage() {
        return groupDamage;
    }

    public void setGroupDamage(Integer groupDamage) {
        this.groupDamage = groupDamage;
    }

    public Integer getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(Integer timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public Integer getTaskOrder() {
        return taskOrder;
    }

    public void setTaskOrder(Integer taskOrder) {
        this.taskOrder = taskOrder;
    }

    public Integer getWindowStartSeconds() {
        return windowStartSeconds;
    }

    public void setWindowStartSeconds(Integer windowStartSeconds) {
        this.windowStartSeconds = windowStartSeconds;
    }

    public List<Long> getCompletedByUserIds() {
        return completedByUserIds;
    }

    public void setCompletedByUserIds(List<Long> completedByUserIds) {
        this.completedByUserIds = completedByUserIds;
    }

    public List<Long> getSuccessfullyCompletedByUsers() {
        return successfullyCompletedByUsers;
    }

    public void setSuccessfullyCompletedByUsers(List<Long> successfullyCompletedByUsers) {
        this.successfullyCompletedByUsers = successfullyCompletedByUsers;
    }
}

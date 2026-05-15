package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;

public class RaidMemberDTO {
    private Long userId;
    private String username;
    private UserStatus online;
    private Boolean joined;
    private Boolean accepted;
    private Integer tasksCompleted;
    private Integer tasksFailed;
    private Integer damageDealt;
    private Integer health;
    private Integer maxHealth;
    private String characterType;
    private Integer xpEarned;
    private Boolean mvp;
    private ItemGetDTO droppedItem;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserStatus getOnline() {
        return online;
    }

    public void setOnline(UserStatus online) {
        this.online = online;
    }

    public Boolean getJoined() {
        return joined;
    }

    public void setJoined(Boolean joined) {
        this.joined = joined;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public Integer getTasksCompleted() {
        return tasksCompleted;
    }

    public void setTasksCompleted(Integer tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }

    public Integer getTasksFailed() {
        return tasksFailed;
    }

    public void setTasksFailed(Integer tasksFailed) {
        this.tasksFailed = tasksFailed;
    }

    public Integer getDamageDealt() {
        return damageDealt;
    }

    public void setDamageDealt(Integer damageDealt) {
        this.damageDealt = damageDealt;
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

    public String getCharacterType() {
        return characterType;
    }

    public void setCharacterType(String characterType) {
        this.characterType = characterType;
    }

    public Integer getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(Integer xpEarned) {
        this.xpEarned = xpEarned;
    }

    public Boolean getMvp() {
        return mvp;
    }

    public void setMvp(Boolean mvp) {
        this.mvp = mvp;
    }

    public ItemGetDTO getDroppedItem() {
        return droppedItem;
    }

    public void setDroppedItem(ItemGetDTO droppedItem) {
        this.droppedItem = droppedItem;
    }
}

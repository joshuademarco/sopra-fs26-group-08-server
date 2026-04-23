package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class LeaderboardEntryDTO {
    private String username;
    private Integer experience;
    private Integer level;

    public LeaderboardEntryDTO() {}

    public LeaderboardEntryDTO(String username, Integer experience, Integer level) {
        this.username = username;
        this.experience = experience;
        this.level = level;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
}
package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class RaidPostDTO {
    private String name;
    private Integer durationSeconds;
    private Integer health;
    /** Days to search for a free slot */
    private Integer searchWindowDays;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(Integer health) {
        this.health = health;
    }

    public Integer getSearchWindowDays() {
        return searchWindowDays;
    }

    public void setSearchWindowDays(Integer searchWindowDays) {
        this.searchWindowDays = searchWindowDays;
    }
}

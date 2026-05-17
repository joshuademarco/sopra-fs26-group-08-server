package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class WeatherQuestGetDTO {
    private String weatherCondition;
    private String weatherLabel;
    private String questTitle;
    private String targetCategory;
    private int targetCount;
    private String bonusStat;
    private double bonusMultiplier;
    private int completedCount;
    private boolean completed;

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    public String getWeatherLabel() {
        return weatherLabel;
    }

    public void setWeatherLabel(String weatherLabel) {
        this.weatherLabel = weatherLabel;
    }

    public String getQuestTitle() {
        return questTitle;
    }

    public void setQuestTitle(String questTitle) {
        this.questTitle = questTitle;
    }

    public String getTargetCategory() {
        return targetCategory;
    }

    public void setTargetCategory(String targetCategory) {
        this.targetCategory = targetCategory;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(int targetCount) {
        this.targetCount = targetCount;
    }

    public String getBonusStat() {
        return bonusStat;
    }

    public void setBonusStat(String bonusStat) {
        this.bonusStat = bonusStat;
    }

    public double getBonusMultiplier() {
        return bonusMultiplier;
    }

    public void setBonusMultiplier(double bonusMultiplier) {
        this.bonusMultiplier = bonusMultiplier;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

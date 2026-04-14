package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import ch.uzh.ifi.hase.soprafs26.constant.HabitFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HabitPostDTO {

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotNull(message = "Category is required")
    private HabitCategory category;

    @NotNull(message = "Frequency is required")
    private HabitFrequency frequency;

    @NotNull(message = "Please specify if this is a positive or negative habit")
    private Boolean positive;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public HabitCategory getCategory() { return category; }
    public void setCategory(HabitCategory category) { this.category = category; }

    public HabitFrequency getFrequency() { return frequency; }
    public void setFrequency(HabitFrequency frequency) { this.frequency = frequency; }

    public Boolean getPositive() { return positive; }
    public void setPositive(Boolean positive) { this.positive = positive; }
}

package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class TodoPostDTO {

    @NotBlank(message = "Title cannot be empty")
    private String title;

    //optional description
    private String description;

    @NotNull(message = "Category is required")
    private HabitCategory category;

    //weight 1-3 (default 1 -> easy)
    private Integer weight = 1;

    private Instant dueAt;

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

    public HabitCategory getCategory() {
        return category;
    }

    public void setCategory(HabitCategory category) {
        this.category = category;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public void setDueAt(Instant dueAt) {
        this.dueAt = dueAt;
    }
}

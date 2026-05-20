package ch.uzh.ifi.hase.soprafs26.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class HabitCompletionEventEntityTest {

    private HabitCompletionEvent event;

    @BeforeEach
    public void setup() {
        event = new HabitCompletionEvent();
    }

    @Test
    public void defaultWeatherMultiplier_is1dot0() {
        assertEquals(1.0, event.getWeatherMultiplier());
    }

    @Test
    public void setAndGetId() {
        event.setId(42L);
        assertEquals(42L, event.getId());
    }

    @Test
    public void setAndGetHabit() {
        Habit habit = new Habit();
        habit.setId(10L);
        event.setHabit(habit);
        assertEquals(habit, event.getHabit());
    }

    @Test
    public void setAndGetUser() {
        User user = new User();
        user.setId(5L);
        event.setUser(user);
        assertEquals(user, event.getUser());
    }

    @Test
    public void setAndGetCompletedAt() {
        Instant now = Instant.now();
        event.setCompletedAt(now);
        assertEquals(now, event.getCompletedAt());
    }

    @Test
    public void setAndGetBaseXp() {
        event.setBaseXp(50);
        assertEquals(50, event.getBaseXp());
    }

    @Test
    public void setAndGetWeatherMultiplier() {
        event.setWeatherMultiplier(1.5);
        assertEquals(1.5, event.getWeatherMultiplier());
    }

    @Test
    public void setAndGetMultipliedXp() {
        event.setMultipliedXp(75);
        assertEquals(75, event.getMultipliedXp());
    }

    @Test
    public void setAndGetWeatherCode() {
        event.setWeatherCode(45);
        assertEquals(45, event.getWeatherCode());
    }

    @Test
    public void onCreate_setsCompletedAtWhenNull() {
        assertNull(event.getCompletedAt());
        event.onCreate();
        assertNotNull(event.getCompletedAt());
    }

    @Test
    public void weatherCode_nullByDefault() {
        assertNull(event.getWeatherCode());
    }
}

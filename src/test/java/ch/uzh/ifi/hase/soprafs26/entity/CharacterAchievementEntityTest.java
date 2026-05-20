package ch.uzh.ifi.hase.soprafs26.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterAchievementEntityTest {

    private CharacterAchievement ca;

    @BeforeEach
    public void setup() {
        ca = new CharacterAchievement();
    }

    @Test
    public void setAndGetId() {
        ca.setId(1L);
        assertEquals(1L, ca.getId());
    }

    @Test
    public void setAndGetCharacter() {
        Character character = new Character();
        character.setId(10L);
        ca.setCharacter(character);
        assertEquals(character, ca.getCharacter());
    }

    @Test
    public void setAndGetAchievement() {
        Achievement achievement = new Achievement();
        achievement.setId(5L);
        achievement.setName("First Habit");
        ca.setAchievement(achievement);
        assertEquals(achievement, ca.getAchievement());
        assertEquals("First Habit", ca.getAchievement().getName());
    }

    @Test
    public void setAndGetEarnedAt() {
        Instant now = Instant.now();
        ca.setEarnedAt(now);
        assertEquals(now, ca.getEarnedAt());
    }

    @Test
    public void onCreate_setsEarnedAt() {
        assertNull(ca.getEarnedAt());
        ca.onCreate();
        assertNotNull(ca.getEarnedAt());
    }

    @Test
    public void onCreate_setsEarnedAtToApproximatelyNow() {
        Instant before = Instant.now();
        ca.onCreate();
        Instant after = Instant.now();
        assertNotNull(ca.getEarnedAt());
        assertFalse(ca.getEarnedAt().isBefore(before));
        assertFalse(ca.getEarnedAt().isAfter(after));
    }
}

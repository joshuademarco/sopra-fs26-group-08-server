package ch.uzh.ifi.hase.soprafs26.entity;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterEntityTest {

    private Character character;

    @BeforeEach
    public void setup() {
        character = new Character();
        character.setLevel(1);
        character.setExperience(0);
        character.setMaxHealth(10);
        character.setHealth(10);
        character.setStrength(1);
        character.setIntelligence(1);
        character.setResilience(1);
    }

    @Test
    public void addExperience_belowThreshold_noLevelUp() {
        character.addExperience(50);

        assertEquals(1, character.getLevel());
        assertEquals(50, character.getExperience());
    }

    @Test
    public void addExperience_exactlyAtThreshold_levelsUp() {
        character.addExperience(100); // threshold at level 1 is 100

        assertEquals(2, character.getLevel());
        assertEquals(0, character.getExperience()); // should not have leftover XP
    }

    @Test
    public void addExperience_overThreshold_carryOverXp() {
        character.addExperience(110);

        assertEquals(2, character.getLevel());
        assertEquals(10, character.getExperience()); // 10 XP carried over
    }

    @Test
    public void addExperience_multipleLevel_whileLoopHandles() {
        // give 350 XP so we level up multiple times and have leftover XP
        // lvl 1 threshold: 100 -> level up, 250 remaining
        // lvl 2 threshold: 200 -> level up, 50 remaining
        // lvl 3 threshold: 300 -> not enough
        character.addExperience(350);

        assertEquals(3, character.getLevel());
        assertEquals(50, character.getExperience());
    }

    @Test
    public void addExperience_levelUp_restoresHealth() {
        character.setHealth(5); // damaged character
        character.addExperience(100);

        // health should be restored to maxHealth on level up
        assertEquals(character.getMaxHealth(), character.getHealth());
    }

    @Test
    public void addExperience_levelUp_increasesMaxHealth() {
        int oldMaxHealth = character.getMaxHealth();
        character.addExperience(100);

        assertTrue(character.getMaxHealth() > oldMaxHealth);
    }

    @Test
    public void getXpThreshold_level1_returns100() {
        character.setLevel(1);
        assertEquals(100, character.getXpThreshold());
    }

    @Test
    public void getXpThreshold_level2_returns200() {
        character.setLevel(2);
        assertEquals(200, character.getXpThreshold());
    }

    @Test
    public void increaseStat_physical_increasesStrength() {
        character.increaseStat(HabitCategory.PHYSICAL);
        assertEquals(2, character.getStrength());
        assertEquals(1, character.getIntelligence()); // stay the same
        assertEquals(1, character.getResilience()); // stay the same
    }

    @Test
    public void increaseStat_cognitive_increasesIntelligence() {
        character.increaseStat(HabitCategory.COGNITIVE);
        assertEquals(2, character.getIntelligence());
        assertEquals(1, character.getStrength());
    }

    @Test
    public void increaseStat_emotional_increasesResilience() {
        character.increaseStat(HabitCategory.EMOTIONAL);
        assertEquals(2, character.getResilience());
        assertEquals(1, character.getStrength());
    }

    @Test
    public void hit_reducesHealth() {
        character.setResilience(0);
        character.hit(5);
        assertEquals(5, character.getHealth()); // 10-5 = 5
    }

    @Test
    public void hit_resilience_reducesActualDamage() {
        character.setResilience(3);
        character.hit(5);
        assertEquals(8, character.getHealth()); // damage = 5-3 = 2, health = 10-2 = 8
    }

    @Test
    public void hit_moreDamageThanHealth_doesNotGoBelowZero() {
        character.hit(100);
        assertEquals(0, character.getHealth());
    }

    @Test
    public void heal_increasesHealth() {
        character.setHealth(5);
        character.heal(3);
        assertEquals(8, character.getHealth());
    }

    @Test
    public void heal_doesNotExceedMaxHealth() {
        character.setHealth(9);
        character.heal(100); // try to overheal
        assertEquals(character.getMaxHealth(), character.getHealth());
    }

    @Test
    public void applyNegativeHabitPenalty_weight1_reducesHealthBy1() {
        character.setHealth(10);
        character.applyNegativeHabitPenalty(1);
        assertEquals(9, character.getHealth());
    }

    @Test
    public void applyNegativeHabitPenalty_weight2_reducesHealthBy2() {
        character.setHealth(10);
        character.applyNegativeHabitPenalty(2);
        assertEquals(8, character.getHealth());
    }

    @Test
    public void applyNegativeHabitPenalty_weight3_reducesHealthBy3() {
        character.setHealth(10);
        character.applyNegativeHabitPenalty(3);
        assertEquals(7, character.getHealth());
    }

    @Test
    public void applyNegativeHabitPenalty_healthCannotGoBelowZero() {
        character.setHealth(1);
        character.applyNegativeHabitPenalty(3);
        // character dies and is reset, hence health back to maxHealth=10 instead of -2
        assertEquals(10, character.getHealth());
    }

    @Test
    public void addExperience_levelUp_restoresFullHealth() {
        character.setHealth(3);
        character.addExperience(100);
        assertEquals(character.getMaxHealth(), character.getHealth());
    }
}

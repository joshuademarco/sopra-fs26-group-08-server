package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.CharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CharacterServiceTest {

    @Mock
    private CharacterRepository characterRepository;

    @InjectMocks
    private CharacterService characterService;

    private Character testCharacter;
    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testCharacter = new Character();
        testCharacter.setId(1L);
        testCharacter.setUser(testUser);
        testCharacter.setLevel(1);
        testCharacter.setExperience(0);
        testCharacter.setMaxHealth(10);
        testCharacter.setHealth(10);
        testCharacter.setStrength(1);
        testCharacter.setIntelligence(1);
        testCharacter.setResilience(1);
    }

    // ---------------tests for calculateBaseXp---------------
    @Test
    public void calculateBaseXp_weight1_returns10() {
        int xp = characterService.calculateBaseXp(1);
        assertEquals(10, xp);
    }

    @Test
    public void calculateBaseXp_weight2_returns20() {
        int xp = characterService.calculateBaseXp(2);
        assertEquals(20, xp);
    }

    @Test
    public void calculateBaseXp_weight3_returns30() {
        int xp = characterService.calculateBaseXp(3);
        assertEquals(30, xp);
    }

    @Test
    public void calculateBaseXp_nullWeight_returns10() {
        // null weight should default to 10 (safe fallback)
        int xp = characterService.calculateBaseXp(null);
        assertEquals(10, xp);
    }

    @Test
    public void calculateBaseXp_zeroWeight_returns10() {
        int xp = characterService.calculateBaseXp(0);
        assertEquals(10, xp);
    }

    // ---------------tests for applyWeatherMultiplier---------------
    @Test
    public void applyWeatherMultiplier_noMultiplier_returnsSameXp() {
        int result = characterService.applyWeatherMultiplier(20, 1.0);
        assertEquals(20, result);
    }

    @Test
    public void applyWeatherMultiplier_1point5x_returnsRoundedXp() {
        int result = characterService.applyWeatherMultiplier(20, 1.5);
        assertEquals(30, result);
    }

    @Test
    public void applyWeatherMultiplier_2x_doublesXp() {
        int result = characterService.applyWeatherMultiplier(10, 2.0);
        assertEquals(20, result);
    }

    // ---------------tests for awardXp---------------
    @Test
    public void awardXp_physicalHabit_increasesStrength() {
        // given
        when(characterRepository.findByUserId(1L)).thenReturn(testCharacter);
        when(characterRepository.save(any())).thenReturn(testCharacter);

        // when
        characterService.awardXp(1L, HabitCategory.PHYSICAL, 10, 1.0);

        // then: strength increased
        assertEquals(2, testCharacter.getStrength());
        assertEquals(10, testCharacter.getExperience());
        // level stays at 1 (need 100 XP to level up)
        assertEquals(1, testCharacter.getLevel());
    }

    @Test
    public void awardXp_cognitiveHabit_increasesIntelligence() {
        when(characterRepository.findByUserId(1L)).thenReturn(testCharacter);
        when(characterRepository.save(any())).thenReturn(testCharacter);

        characterService.awardXp(1L, HabitCategory.COGNITIVE, 10, 1.0);

        assertEquals(2, testCharacter.getIntelligence());
        assertEquals(1, testCharacter.getStrength()); // strength unchanged
    }

    @Test
    public void awardXp_emotionalHabit_increasesResilience() {
        when(characterRepository.findByUserId(1L)).thenReturn(testCharacter);
        when(characterRepository.save(any())).thenReturn(testCharacter);

        characterService.awardXp(1L, HabitCategory.EMOTIONAL, 10, 1.0);

        assertEquals(2, testCharacter.getResilience());
    }

    @Test
    public void awardXp_withWeatherMultiplier_appliesMultiplier() {
        when(characterRepository.findByUserId(1L)).thenReturn(testCharacter);
        when(characterRepository.save(any())).thenReturn(testCharacter);

        // base 20XP * 1.5 weather = 30XP
        int finalXp = characterService.awardXp(1L, HabitCategory.PHYSICAL, 20, 1.5);

        assertEquals(30, finalXp);
        assertEquals(30, testCharacter.getExperience());
    }

    @Test
    public void awardXp_enoughXpToLevelUp_characterLevelsUp() {
        // give character 90XP already —> one more award of 20 should push to level 2
        testCharacter.setExperience(90);
        when(characterRepository.findByUserId(1L)).thenReturn(testCharacter);
        when(characterRepository.save(any())).thenReturn(testCharacter);

        characterService.awardXp(1L, HabitCategory.PHYSICAL, 20, 1.0);

        // 90 + 20 = 110XP, threshold at level 1 is 100 so level up to 2
        assertEquals(2, testCharacter.getLevel());
        // leftover XP = 110 - 100 = 10
        assertEquals(10, testCharacter.getExperience());
        // health restored on level up
        assertEquals(testCharacter.getMaxHealth(), testCharacter.getHealth());
    }

    @Test
    public void awardXp_userNotFound_throwsNotFound() {
        when(characterRepository.findByUserId(99L)).thenReturn(null);

        assertThrows(ResponseStatusException.class,
                () -> characterService.awardXp(99L, HabitCategory.PHYSICAL, 10, 1.0));
    }

    // ---------------tests for getXpProgressPercent---------------
    @Test
    public void getXpProgressPercent_halfwayToNextLevel_returns50() {
        testCharacter.setExperience(50); // 50/100 = 50%
        when(characterRepository.findByUserId(1L)).thenReturn(testCharacter);

        int percent = characterService.getXpProgressPercent(1L);

        assertEquals(50, percent);
    }

    @Test
    public void getXpProgressPercent_noXp_returns0() {
        testCharacter.setExperience(0);
        when(characterRepository.findByUserId(1L)).thenReturn(testCharacter);

        int percent = characterService.getXpProgressPercent(1L);

        assertEquals(0, percent);
    }

    // ---------------tests for createCharacter---------------
    @Test
    public void createCharacter_newUser_setsDefaultValues() {
        when(characterRepository.save(any())).thenReturn(testCharacter);

        Character created = characterService.createCharacter(testUser);

        verify(characterRepository, times(1)).save(any());
        assertNotNull(created);
    }
}

package ch.uzh.ifi.hase.soprafs26.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import ch.uzh.ifi.hase.soprafs26.constant.AchievementKey;
import ch.uzh.ifi.hase.soprafs26.entity.Achievement;
import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.CharacterAchievement;
import ch.uzh.ifi.hase.soprafs26.repository.AchievementRepository;
import ch.uzh.ifi.hase.soprafs26.repository.CharacterAchievementRepository;
import ch.uzh.ifi.hase.soprafs26.repository.CharacterRepository;

public class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private CharacterAchievementRepository characterAchievementRepository;

    @Mock
    private CharacterRepository characterRepository;

    @InjectMocks
    private AchievementService achievementService;

    private Character testCharacter;
    private Achievement testAchievement;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testCharacter = new Character();
        testCharacter.setId(1L);

        testAchievement = new Achievement();
        testAchievement.setId(1L);
        testAchievement.setKey(AchievementKey.FIRST_HABIT);
        testAchievement.setName("Baby Steps");
        testAchievement.setDescription("Complete your first habit");
        testAchievement.setIcon("first_habit");

        when(characterRepository.findByUserId(1L)).thenReturn(testCharacter);
        when(achievementRepository.findByKey(any())).thenReturn(Optional.of(testAchievement));
        when(characterAchievementRepository.existsByCharacterAndAchievement(any(), any())).thenReturn(false);
    }

    @Test
    public void checkHabitAchievements_characterNotFound_doesNothing() {
        when(characterRepository.findByUserId(1L)).thenReturn(null);

        achievementService.checkHabitAchievements(1L, 0);

        verify(characterAchievementRepository, never()).save(any());
    }

    @Test
    public void checkHabitAchievements_firstHabit_awardsAchievement() {
        achievementService.checkHabitAchievements(1L, 0);

        verify(characterAchievementRepository, times(1)).save(any());
    }

    @Test
    public void checkHabitAchievements_alreadyHasAchievement_doesNotSaveAgain() {
        when(characterAchievementRepository.existsByCharacterAndAchievement(any(), any())).thenReturn(true);

        achievementService.checkHabitAchievements(1L, 0);

        verify(characterAchievementRepository, never()).save(any());
    }

    @Test
    public void checkHabitAchievements_streak3_awardsTwoAchievements() {
        Achievement streak3Achievement = new Achievement();
        streak3Achievement.setId(2L);
        streak3Achievement.setKey(AchievementKey.STREAK_3);
        streak3Achievement.setName("On a Roll");
        streak3Achievement.setDescription("Maintain a 3-day streak on any habit");
        streak3Achievement.setIcon("streak_3");

        when(achievementRepository.findByKey(AchievementKey.FIRST_HABIT)).thenReturn(Optional.of(testAchievement));
        when(achievementRepository.findByKey(AchievementKey.STREAK_3)).thenReturn(Optional.of(streak3Achievement));

        achievementService.checkHabitAchievements(1L, 3);

        verify(characterAchievementRepository, times(2)).save(any());
    }

    @Test
    public void checkHabitAchievements_streak7_awardsThreeAchievements() {
        Achievement streak3Achievement = new Achievement();
        streak3Achievement.setId(2L);
        streak3Achievement.setKey(AchievementKey.STREAK_3);
        streak3Achievement.setName("On a Roll");
        streak3Achievement.setDescription("Maintain a 3-day streak on any habit");
        streak3Achievement.setIcon("streak_3");

        Achievement streak7Achievement = new Achievement();
        streak7Achievement.setId(3L);
        streak7Achievement.setKey(AchievementKey.STREAK_7);
        streak7Achievement.setName("Unstoppable");
        streak7Achievement.setDescription("Maintain a 7-day streak on any habit");
        streak7Achievement.setIcon("streak_7");

        when(achievementRepository.findByKey(AchievementKey.FIRST_HABIT)).thenReturn(Optional.of(testAchievement));
        when(achievementRepository.findByKey(AchievementKey.STREAK_3)).thenReturn(Optional.of(streak3Achievement));
        when(achievementRepository.findByKey(AchievementKey.STREAK_7)).thenReturn(Optional.of(streak7Achievement));

        achievementService.checkHabitAchievements(1L, 7);

        verify(characterAchievementRepository, times(3)).save(any());
    }

    @Test
    public void checkStatAchievements_below25_doesNotAward() {
        achievementService.checkStatAchievements(1L, AchievementKey.STRENGTH_25, 24);

        verify(characterAchievementRepository, never()).save(any());
    }

    @Test
    public void checkStatAchievements_at25_awardsAchievement() {
        Achievement strength25 = new Achievement();
        strength25.setId(4L);
        strength25.setKey(AchievementKey.STRENGTH_25);
        strength25.setName("Emerging Warrior");
        strength25.setDescription("Reach 25 Strength");
        strength25.setIcon("strength_25");

        when(achievementRepository.findByKey(AchievementKey.STRENGTH_25)).thenReturn(Optional.of(strength25));

        achievementService.checkStatAchievements(1L, AchievementKey.STRENGTH_25, 25);

        verify(characterAchievementRepository, times(1)).save(any());
    }

    @Test
    public void checkStatAchievements_characterNotFound_doesNothing() {
        when(characterRepository.findByUserId(1L)).thenReturn(null);

        achievementService.checkStatAchievements(1L, AchievementKey.STRENGTH_25, 25);

        verify(characterAchievementRepository, never()).save(any());
    }

    @Test
    public void getAchievementsForUser_characterNotFound_returnsEmpty() {
        when(characterRepository.findByUserId(1L)).thenReturn(null);

        List<CharacterAchievement> result = achievementService.getAchievementsForUser(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    public void getAchievementsForUser_returnsAchievements() {
        CharacterAchievement ca = new CharacterAchievement();
        ca.setId(1L);
        ca.setCharacter(testCharacter);
        ca.setAchievement(testAchievement);

        when(characterAchievementRepository.findByCharacter(testCharacter)).thenReturn(List.of(ca));

        List<CharacterAchievement> result = achievementService.getAchievementsForUser(1L);

        assertEquals(1, result.size());
    }
}

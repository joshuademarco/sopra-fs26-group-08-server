package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.constant.AchievementKey;
import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AchievementService {

    private final Logger log = LoggerFactory.getLogger(AchievementService.class);

    private final AchievementRepository achievementRepository;
    private final CharacterAchievementRepository characterAchievementRepository;
    private final CharacterRepository characterRepository;

    public AchievementService(AchievementRepository achievementRepository,
            CharacterAchievementRepository characterAchievementRepository,
            CharacterRepository characterRepository) {
        this.achievementRepository = achievementRepository;
        this.characterAchievementRepository = characterAchievementRepository;
        this.characterRepository = characterRepository;
    }

    // seed all achievements into DB on startup if they don't exist yet 
    @PostConstruct
    public void seedAchievements() {
        seed(AchievementKey.FIRST_HABIT, "Baby Steps", "Complete your first habit", "first_habit");
        seed(AchievementKey.STREAK_3, "On a Roll", "Maintain a 3-day streak on any habit", "streak_3");
        seed(AchievementKey.STREAK_7, "Unstoppable", "Maintain a 7-day streak on any habit", "streak_7");
        // seed(AchievementKey.FIRST_BOSS, "Ready for Battle", "Help defeat your first
        // bossraid", "first_boss");
        seed(AchievementKey.STRENGTH_25, "Emerging Warrior", "Reach 25 Strength", "strength_25");
        seed(AchievementKey.INTELLIGENCE_25, "Sharp Mind", "Reach 25 Intelligence", "intelligence_25");
        seed(AchievementKey.RESILIENCE_25, "Iron Skin", "Reach 25 Resilience", "resilience_25");
    }

    private void seed(AchievementKey key, String name, String description, String icon) {
        if (achievementRepository.findByKey(key).isEmpty()) {
            Achievement a = new Achievement();
            a.setKey(key);
            a.setName(name);
            a.setDescription(description);
            a.setIcon(icon);
            achievementRepository.save(a);
            log.info("Seeded achievement: {}", key);
        }
    }

    public void checkHabitAchievements(Long userId, int currentStreak) {
        Character character = characterRepository.findByUserId(userId);
        if (character == null)
            return;
        award(character, AchievementKey.FIRST_HABIT);

        if (currentStreak >= 3) {
            award(character, AchievementKey.STREAK_3);
        }
        if (currentStreak >= 7) {
            award(character, AchievementKey.STREAK_7);
        }
    }

    /*
     * boss raids not complete yet
     * 
     * public void checkRaidAchievements(List<Long> participantUserIds) {
     * for (Long userId : participantUserIds) {
     * Character character = characterRepository.findByUserId(userId);
     * if (character == null)
     * continue;
     * award(character, AchievementKey.FIRST_BOSS);
     * }
     * }
     */
    public void checkStatAchievements(Long userId, AchievementKey key, int newStatValue) {
        if (newStatValue < 25)
            return;
        Character character = characterRepository.findByUserId(userId);
        if (character == null)
            return;
        award(character, key);
    }

    // awards an achievement if the character hasn't earned it yet
    private void award(Character character, AchievementKey key) {
        Achievement achievement = achievementRepository.findByKey(key)
                .orElse(null);
        if (achievement == null)
            return;

        if (!characterAchievementRepository.existsByCharacterAndAchievement(character, achievement)) {
            CharacterAchievement characterAchievement = new CharacterAchievement();
            characterAchievement.setCharacter(character);
            characterAchievement.setAchievement(achievement);
            characterAchievementRepository.save(characterAchievement);
            log.info("Achievement '{}' awarded to character {}", key, character.getId());
        }
    }

    public List<CharacterAchievement> getAchievementsForUser(Long userId) {
        Character character = characterRepository.findByUserId(userId);
        if (character == null)
            return List.of();
        return characterAchievementRepository.findByCharacter(character);
    }
}
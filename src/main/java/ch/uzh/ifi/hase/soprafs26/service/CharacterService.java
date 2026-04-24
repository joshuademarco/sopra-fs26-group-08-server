package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.CharacterRepository;

@Service
@Transactional
public class CharacterService {

    private final Logger log = LoggerFactory.getLogger(CharacterService.class);
    private final CharacterRepository characterRepository;

    public CharacterService(@Qualifier("characterRepository") CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    public Character createCharacter(User user) {
        Character newCharacter = new Character();
        newCharacter.setUser(user);

        newCharacter.setLevel(1);
        newCharacter.setHealth(10);
        newCharacter.setMaxHealth(10);
        newCharacter.setExperience(0);
        newCharacter.setStrength(1);
        newCharacter.setResilience(1);
        newCharacter.setIntelligence(1);
        newCharacter.setType("josh");
        newCharacter.setSkinColor("default");

        newCharacter = characterRepository.save(newCharacter);
        characterRepository.flush();

        log.debug("Created Character for User: {}", user.getUsername());
        return newCharacter;
    }

    // calculate habit XP based on difficulty weight (default 1, min 1)
    public int calculateBaseXp(Integer weight) {
        if (weight == null || weight < 1)
            return 10;
        return weight * 10;
    }

    public int applyWeatherMultiplier(int baseXp, double multiplier) {
        return (int) Math.round(baseXp * multiplier);
    }

    // after completing habti/todo -> award XP and update character
    public int awardXp(Long userId, HabitCategory category, int baseXp, double weatherMultiplier) {
        Character character = getCharacterByUserId(userId);

        int finalXp = applyWeatherMultiplier(baseXp, weatherMultiplier);

        // level up and stats are handled in Character.java
        character.addExperience(finalXp);
        character.increaseStat(category);

        characterRepository.save(character);

        log.debug("Awarded {} XP (base: {}, multiplier: {}) to user {}",
                finalXp, baseXp, weatherMultiplier, userId);

        return finalXp;
    }

    public void applyNegativeHabitPenalty(Long userId, Integer weight) {
        Character character = getCharacterByUserId(userId);
        character.applyNegativeHabitPenalty(weight);
        characterRepository.save(character);
        log.debug("Applied negative habit penalty (weight={}) to user {}", weight, userId);
    }

    // will be useful for frontend to show XP progress towards next level
    public int getXpProgressPercent(Long userId) {
        Character character = getCharacterByUserId(userId);
        int threshold = character.getXpThreshold();
        if (threshold == 0) // avoid division by zero just in case
            return 100;
        return (int) ((character.getExperience() * 100.0) / threshold);
    }

    public Character getCharacterByUserId(Long userId) {
        Character character = characterRepository.findByUserId(userId);
        if (character == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Character for this user was not found");
        }
        return character;
    }
}
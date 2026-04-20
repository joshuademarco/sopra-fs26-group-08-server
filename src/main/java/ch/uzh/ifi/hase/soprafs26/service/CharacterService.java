package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.CharacterRepository;

@Service
@Transactional
public class CharacterService {

    private final Logger log = LoggerFactory.getLogger(CharacterService.class);
    private final CharacterRepository characterRepository;

    @Autowired
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

    public Character getCharacterByUserId(Long userId) {
        Character character = characterRepository.findByUserId(userId);
        if (character == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Character for this user was not found");
        }
        return character;
    }
}
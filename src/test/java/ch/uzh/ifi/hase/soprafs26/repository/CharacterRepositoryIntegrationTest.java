package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class CharacterRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CharacterRepository characterRepository;

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password123");
        user.setToken("token-" + username);
        user.setStatus(UserStatus.ONLINE);
        user.setOnline(true);
        return (User) entityManager.persist(user);
    }

    private Character createCharacter(User user) {
        Character character = new Character();
        character.setUser(user);
        character.setLevel(1);
        character.setHealth(10);
        character.setMaxHealth(10);
        character.setExperience(0);
        character.setStrength(1);
        character.setIntelligence(1);
        character.setResilience(1);
        character.setSkinColor("default");
        character.setType("warrior");
        return (Character) entityManager.persist(character);
    }

    @Test
    public void findByUserId_existingUser_returnsCharacter() {
        User user = createUser("charUser", "char@test.com");
        Character character = createCharacter(user);
        entityManager.flush();

        Character found = characterRepository.findByUserId(user.getId());

        assertNotNull(found);
        assertEquals(character.getId(), found.getId());
        assertEquals(user.getId(), found.getUser().getId());
    }

    @Test
    public void findByUserId_noCharacter_returnsNull() {
        User user = createUser("noCharUser", "nochar@test.com");
        entityManager.flush();

        Character found = characterRepository.findByUserId(user.getId());

        assertNull(found);
    }

    @Test
    public void persistCharacter_allFieldsSavedCorrectly() {
        User user = createUser("persistUser", "persist@test.com");
        Character character = createCharacter(user);
        entityManager.flush();
        entityManager.clear(); // force DB read, not cache

        Character found = characterRepository.findById(character.getId()).orElseThrow();

        assertEquals(1, found.getLevel());
        assertEquals(10, found.getHealth());
        assertEquals(10, found.getMaxHealth());
        assertEquals(0, found.getExperience());
        assertEquals(1, found.getStrength());
        assertEquals(1, found.getIntelligence());
        assertEquals(1, found.getResilience());
        assertEquals("default", found.getSkinColor());
        assertEquals("warrior", found.getType());
        assertNotNull(found.getUpdatedAt()); // set by @PrePersist
        assertNotNull(found.getUser());
        assertEquals(user.getId(), found.getUser().getId());
    }

    @Test
    public void updateCharacter_experienceUpdated_persistsCorrectly() {
        User user = createUser("xpUser", "xp@test.com");
        Character character = createCharacter(user);
        entityManager.flush();

        // update experience
        character.setExperience(50);
        characterRepository.save(character);
        entityManager.flush();
        entityManager.clear();

        Character found = characterRepository.findByUserId(user.getId());
        assertEquals(50, found.getExperience());
    }

    @Test
    public void updateCharacter_levelUp_persistsNewLevel() {
        User user = createUser("levelUser", "level@test.com");
        Character character = createCharacter(user);
        entityManager.flush();

        character.addExperience(100); // triggers level up
        characterRepository.save(character);
        entityManager.flush();
        entityManager.clear();

        Character found = characterRepository.findByUserId(user.getId());
        assertEquals(2, found.getLevel());
        assertEquals(0, found.getExperience()); // XP reset after level up
        assertEquals(15, found.getMaxHealth()); // 10 + 5
    }
}

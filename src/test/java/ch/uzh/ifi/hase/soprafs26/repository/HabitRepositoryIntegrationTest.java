package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import ch.uzh.ifi.hase.soprafs26.constant.HabitFrequency;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Habit;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class HabitRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HabitRepository habitRepository;

    // helper to create and persist a user
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

    private Habit createHabit(String title, User user, HabitCategory category) {
        Habit habit = new Habit();
        habit.setTitle(title);
        habit.setCategory(category);
        habit.setFrequency(HabitFrequency.DAILY);
        habit.setPositive(true);
        habit.setWeight(1);
        habit.setUser(user);
        habit.setCompleted(false);
        habit.setStreak(0);
        return (Habit) entityManager.persist(habit);
    }

    @Test
    public void findByUserId_returnsHabitsForCorrectUser() {
        // given -> two users each with habits
        User user1 = createUser("user1", "user1@test.com");
        User user2 = createUser("user2", "user2@test.com");

        createHabit("Morning Run", user1, HabitCategory.PHYSICAL);
        createHabit("Read Book", user1, HabitCategory.COGNITIVE);
        createHabit("Meditate", user2, HabitCategory.EMOTIONAL);

        entityManager.flush();

        // when
        List<Habit> user1Habits = habitRepository.findByUserId(user1.getId());
        List<Habit> user2Habits = habitRepository.findByUserId(user2.getId());

        // then
        assertEquals(2, user1Habits.size());
        assertEquals(1, user2Habits.size());
        // verify correct habits returned for user1
        assertTrue(user1Habits.stream().anyMatch(h -> h.getTitle().equals("Morning Run")));
        assertTrue(user1Habits.stream().anyMatch(h -> h.getTitle().equals("Read Book")));
        // verify user2's habit is not in user1's list
        assertFalse(user1Habits.stream().anyMatch(h -> h.getTitle().equals("Meditate")));
    }

    @Test
    public void persistHabit_savedCorrectly_allFieldsPersisted() {
        // given
        User user = createUser("habitUser", "habit@test.com");
        Habit habit = createHabit("Push-ups", user, HabitCategory.PHYSICAL);
        entityManager.flush();
        entityManager.clear(); // clear cache so we read from DB -> not memory

        // when —> reload from DB
        Habit found = habitRepository.findById(habit.getId()).orElseThrow();

        // then —> verify all fields are correctly persisted and loaded
        assertEquals("Push-ups", found.getTitle());
        assertEquals(HabitCategory.PHYSICAL, found.getCategory());
        assertEquals(HabitFrequency.DAILY, found.getFrequency());
        assertTrue(found.getPositive());
        assertEquals(1, found.getWeight());
        assertFalse(found.getCompleted());
        assertEquals(0, found.getStreak());
        assertNotNull(found.getCreatedAt()); // set by @PrePersist
        assertNotNull(found.getUser());
        assertEquals(user.getId(), found.getUser().getId());
    }

    @Test
    public void findByUser_Id_userWithNoHabits_returnsEmptyList() {
        User user = createUser("emptyUser", "empty@test.com");
        entityManager.flush();

        List<Habit> habits = habitRepository.findByUserId(user.getId());

        assertTrue(habits.isEmpty());
    }
}

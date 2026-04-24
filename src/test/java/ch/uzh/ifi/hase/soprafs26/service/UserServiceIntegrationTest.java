package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.CharacterRepository;
import ch.uzh.ifi.hase.soprafs26.repository.HabitCompletionEventRepository;
import ch.uzh.ifi.hase.soprafs26.repository.HabitRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RaidParticipationRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RaidTaskRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TodoRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

	@Qualifier("userRepository")
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@Autowired
    private CharacterRepository characterRepository;

	@Autowired
    private HabitCompletionEventRepository habitCompletionEventRepository;

	@Autowired
    private HabitRepository habitRepository;

	@Autowired
    private RaidParticipationRepository raidParticipationRepository;

	@Autowired
    private RaidTaskRepository raidTaskRepository;

	@Autowired
    private TodoRepository todoRepository;

	@BeforeEach
    public void setup() {
        raidTaskRepository.deleteAll();
        raidParticipationRepository.deleteAll();
        habitCompletionEventRepository.deleteAll();
        habitRepository.deleteAll();
        todoRepository.deleteAll();
        characterRepository.deleteAll();
        userRepository.deleteAll();
    }

	@AfterEach
    public void teardown() {
        raidTaskRepository.deleteAll();
        raidParticipationRepository.deleteAll();
        habitCompletionEventRepository.deleteAll();
        habitRepository.deleteAll();
        todoRepository.deleteAll();
        characterRepository.deleteAll();
        userRepository.deleteAll();
    }

	@Test
	public void createUser_validInputs_success() {
		// given
		assertNull(userRepository.findByUsername("testUsername"));

		User testUser = new User();
		testUser.setEmail("testemail@uzh.ch");
		testUser.setPassword("password123");
		testUser.setUsername("testUsername");

		// when
		User createdUser = userService.createUser(testUser);

		// then
		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getEmail(), createdUser.getEmail());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
	}

	@Test
	public void createUser_duplicateUsername_throwsException() {
		assertNull(userRepository.findByUsername("testUsername"));

		User testUser = new User();
		testUser.setEmail("testemail@uzh.ch");
		testUser.setPassword("password123");
		testUser.setUsername("testUsername");
		userService.createUser(testUser);

		// attempt to create second user with same username
		User testUser2 = new User();
		// change the email but forget about the username
		testUser2.setEmail("testemail@uzh.ch");

		testUser2.setPassword("password123");
		testUser2.setUsername("testUsername");

		// check that an error is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
	}
}

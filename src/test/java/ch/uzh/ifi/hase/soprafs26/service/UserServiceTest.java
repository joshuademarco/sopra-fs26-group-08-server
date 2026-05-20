package ch.uzh.ifi.hase.soprafs26.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.CharacterRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.LeaderboardEntryDTO;

public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
    private CharacterService characterService;

	@Mock
	private CharacterRepository characterRepository;

	@InjectMocks
	private UserService userService;

	private User testUser;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		// given
		testUser = new User();
		testUser.setId(1L);
		testUser.setUsername("testUsername");
		testUser.setEmail("testemail@uzh.ch");
		testUser.setPassword("password123");

		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		Mockito.when(characterService.createCharacter(any(), any())).thenReturn(new Character());
        Mockito.when(userRepository.save(any())).thenReturn(testUser);
	}

	@Test
	public void createUser_validInputs_success() {
		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		User createdUser = userService.createUser(testUser);

		// then
		verify(userRepository, times(1)).save(any());
        verify(characterService, times(1)).createCharacter(any(), any());

		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getEmail(), createdUser.getEmail());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.OFFLINE, createdUser.getStatus());
	}

	@Test
	public void createUser_duplicateName_throwsException() {
		// given -> a first user has already been created
		userService.createUser(testUser);

		// when -> setup additional mocks for UserRepository
		Mockito.when(userRepository.findByEmail(Mockito.any())).thenReturn(testUser);
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

		// then -> attempt to create second user with same user -> check that an error
		// is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

	@Test
	public void createUser_duplicateInputs_throwsException() {
		// given -> a first user has already been created
		userService.createUser(testUser);

		// when -> setup additional mocks for UserRepository
		Mockito.when(userRepository.findByEmail(Mockito.any())).thenReturn(testUser);
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

		// then -> attempt to create second user with same user -> check that an error
		// is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

    @Test
    public void getLeaderboard_returnsSortedAndFilteredLeaderboard() {
        User userLow = new User();
        userLow.setUsername("lowUser");
        Character lowCharacter = new Character();
        lowCharacter.setExperience(10);
        lowCharacter.setLevel(1);
        userLow.setCharacter(lowCharacter);

        User userHighA = new User();
        userHighA.setUsername("alphaUser");
        Character highCharacterA = new Character();
        highCharacterA.setExperience(250);
        highCharacterA.setLevel(5);
        userHighA.setCharacter(highCharacterA);
        userHighA.setToken("testToken");

        User userHighB = new User();
        userHighB.setUsername("betaUser");
        Character highCharacterB = new Character();
        highCharacterB.setExperience(250);
        highCharacterB.setLevel(5);
        userHighB.setCharacter(highCharacterB);

        User noCharacterUser = new User();
        noCharacterUser.setUsername("noCharacterUser");

        Mockito.when(userRepository.findAll()).thenReturn(Arrays.asList(userLow, noCharacterUser, userHighB, userHighA));
        Mockito.when(userRepository.findByToken("testToken")).thenReturn(userHighA);

        List<LeaderboardEntryDTO> leaderboard = userService.getLeaderboard();

        assertEquals(3, leaderboard.size());
        assertEquals("alphaUser", leaderboard.get(0).getUsername());
        assertEquals(250, leaderboard.get(0).getExperience());
        assertEquals(5, leaderboard.get(0).getLevel());
        assertEquals("betaUser", leaderboard.get(1).getUsername());
        assertEquals(250, leaderboard.get(1).getExperience());
        assertEquals(5, leaderboard.get(1).getLevel());
        assertEquals("lowUser", leaderboard.get(2).getUsername());
        assertEquals(10, leaderboard.get(2).getExperience());
        assertEquals(1, leaderboard.get(2).getLevel());
    }

    @Test
    public void getLeaderboard_noUsers_returnsEmptyList() {
        Mockito.when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<LeaderboardEntryDTO> leaderboard = userService.getLeaderboard();

        assertTrue(leaderboard.isEmpty());
    }

    @Test
    public void getLeaderboard_usersWithoutCharacters_returnsEmptyList() {
        User noCharacterUser1 = new User();
        noCharacterUser1.setUsername("noCharacterUser1");
        User noCharacterUser2 = new User();
        noCharacterUser2.setUsername("noCharacterUser2");

        Mockito.when(userRepository.findAll()).thenReturn(Arrays.asList(noCharacterUser1, noCharacterUser2));

        List<LeaderboardEntryDTO> leaderboard = userService.getLeaderboard();

        assertTrue(leaderboard.isEmpty());
    }

    @Test
    public void getLeaderboard_moreThan10Users_returnsOnlyTop10() {
        List<User> manyUsers = new ArrayList<>();
        // Create 15 users with incremental levels and experience
        for (int i = 1; i <= 15; i++) {
            User u = new User();
            u.setUsername("user" + i);
            Character c = new Character();
            c.setExperience(i * 10);
            c.setLevel(i);
            u.setCharacter(c);
            manyUsers.add(u);
        }

        Mockito.when(userRepository.findAll()).thenReturn(manyUsers);
        Mockito.when(userRepository.findByToken("testToken")).thenReturn(manyUsers.get(14));

        List<LeaderboardEntryDTO> leaderboard = userService.getLeaderboard();

        assertEquals(10, leaderboard.size(), "Leaderboard should only return the top 10 users");
        assertEquals("user15", leaderboard.get(0).getUsername(), "User with highest XP should be first");
        assertEquals("user6", leaderboard.get(9).getUsername(), "User with 10th highest XP should be last");
        assertTrue(leaderboard.stream().noneMatch(entry -> entry.getUsername().equals("user5")), "User outside top 10 should not be present");
    }

    @Test
    public void getLeaderboard_currentUserNotInTop10_returnsTop10PlusCurrentUser() {
        List<User> manyUsers = new ArrayList<>();
        // Create 15 users with incremental levels and experience
        for (int i = 1; i <= 15; i++) {
            User u = new User();
            u.setId((long) i);
            u.setUsername("user" + i);
            u.setToken("token" + i);
            Character c = new Character();
            c.setExperience(i * 10);
            c.setLevel(i);
            u.setCharacter(c);
            manyUsers.add(u);
        }

        User currentUser = manyUsers.get(4); // user5 has 50 XP, placing them 11th out of 15

        Mockito.when(userRepository.findAll()).thenReturn(manyUsers);
        Mockito.when(userRepository.findByToken("token5")).thenReturn(currentUser);

        // Note: Change the parameter below to match your updated getLeaderboard signature (e.g. userId or token)
        List<LeaderboardEntryDTO> leaderboard = userService.getLeaderboard();

        assertEquals(11, leaderboard.size(), "Leaderboard should return top 10 + current user");
        assertEquals("user15", leaderboard.get(0).getUsername(), "1st place should be user15");
        assertEquals("user6", leaderboard.get(9).getUsername(), "10th place should be user6");
        assertEquals("user5", leaderboard.get(10).getUsername(), "11th entry should be the appended current user");
    }
}

package ch.uzh.ifi.hase.soprafs26.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.CharacterRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.LeaderboardEntryDTO;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

	private final Logger log = LoggerFactory.getLogger(UserService.class);
	private final UserRepository userRepository;
	private final CharacterRepository characterRepository;
	private final CharacterService characterService;

    public UserService(@Qualifier("userRepository") UserRepository userRepository, 
                       CharacterService characterService,
                       CharacterRepository characterRepository) { 
        this.userRepository = userRepository;
        this.characterService = characterService;
        this.characterRepository = characterRepository;
					   }
					   
	public User getUserById(Long id) {
        // Find the user in the database
        Optional<User> userById = userRepository.findById(id);

        // If not found, throw a 404 error so the frontend knows what happened
        if (userById.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                String.format("User with ID %d was not found!", id));
        }

        return userById.get();
    }

	public List<User> getUsers() {
		return this.userRepository.findAll();
	}

	public User createUser(User newUser) {
		newUser.setToken(UUID.randomUUID().toString());
		newUser.setStatus(UserStatus.ONLINE);
		newUser.setOnline(true);
		checkIfUserExists(newUser);

		// hash the password before saving
		newUser.setPassword(hashPassword(newUser.getPassword()));

		newUser.setHealth(100);
        newUser.setMaxHealth(100);
		
		// saves the given entity but data is only persisted in the database once
		// flush() is called
		newUser = userRepository.save(newUser);
		userRepository.flush();

		//create a character for the new user
		Character newCharacter = characterService.createCharacter(newUser);
        newUser.setCharacter(newCharacter);

		log.debug("Created Information for User: {}", newUser);
		return newUser;
	}

	/**
	 * This is a helper method that will check the uniqueness criteria of the
	 * username and the name
	 * defined in the User entity. The method will do nothing if the input is unique
	 * and throw an error otherwise.
	 *
	 * @param userToBeCreated
	 * @throws org.springframework.web.server.ResponseStatusException
	 * @see User
	 */
	private void checkIfUserExists(User userToBeCreated) {
		User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
		User userByEmail = userRepository.findByEmail(userToBeCreated.getEmail());

		if (userByUsername != null || userByEmail != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username or email already exists");
		}
	}

	private String hashPassword(String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(password.getBytes());
			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorithm not found", e);
		}
	}

	public User login(String email, String password) {
		User user = userRepository.findByEmail(email);
		// hash the input password to compare with stored hash
		if (user == null || !user.getPassword().equals(hashPassword(password))) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		}
		user.setStatus(UserStatus.ONLINE);
		user.setOnline(true);
		userRepository.saveAndFlush(user);
		return user;
	}

	public User getUserByToken(String token) {
		if (token == null || token.isBlank()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Token");
		}

		User user = userRepository.findByToken(token);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
		}

		return user;
	}

	public User logout(String token) {
		User user = userRepository.findByToken(token);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
		}

		user.setStatus(UserStatus.OFFLINE);
		user.setOnline(false);
		user.setToken(UUID.randomUUID().toString());
		userRepository.saveAndFlush(user);
		return user;

	}

	public List<LeaderboardEntryDTO> getLeaderboard() {
		List<User> users = userRepository.findAll();
		return users.stream()
			.filter(user -> user.getCharacter() != null)
			.sorted((u1, u2) -> Integer.compare(u2.getCharacter().getExperience(), u1.getCharacter().getExperience()))
			.map(user -> new LeaderboardEntryDTO(user.getUsername(), user.getCharacter().getExperience(), user.getCharacter().getLevel()))
			.toList();
	}
}

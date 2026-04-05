package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

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

	public UserService(@Qualifier("userRepository") UserRepository userRepository) {
		this.userRepository = userRepository;
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
		newUser.setStatus(UserStatus.OFFLINE);
		newUser.setOnline(false);
		checkIfUserExists(newUser);

		//TODO: hash the password before saving it in the database!

		// saves the given entity but data is only persisted in the database once
		// flush() is called
		newUser = userRepository.save(newUser);
		userRepository.flush();

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

	public User login(String email, String password) {
		User user = userRepository.findByEmail(email);
		if (user == null || !user.getPassword().equals(password)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		}
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
}

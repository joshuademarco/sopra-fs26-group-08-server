package ch.uzh.ifi.hase.soprafs26.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final CharacterService characterService;
    private final NotificationService notificationService;

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository,
            CharacterService characterService,
            CharacterRepository characterRepository,
            NotificationService notificationService) {
        this.userRepository = userRepository;
        this.characterService = characterService;
        this.notificationService = notificationService;
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
        return createUser(newUser, null);
    }

    public User createUser(User newUser, String characterType) {
        if (newUser.getUsername().contains(" ")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot contain a whitespace");
        }
        newUser.setToken(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.OFFLINE);
        checkIfUserExists(newUser);
        newUser.setPassword(hashPassword(newUser.getPassword()));

        // saves the given entity but data is only persisted in the database once
        // flush() is called
        newUser = userRepository.save(newUser);
        userRepository.flush();

        // create a character for the new user
        Character newCharacter = characterService.createCharacter(newUser, characterType);
        newUser.setCharacter(newCharacter);
        notificationService.sendWelcomeEmail(newUser);

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
                if (hex.length() == 1)
                    hexString.append('0');
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
        userRepository.saveAndFlush(user);
        return user;
    }

    public User changePassword(String token, String currentPassword, String newPassword) {
        User user = getUserByToken(token);
        if (!user.getPassword().equals(hashPassword(currentPassword))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid current password");
        }
        user.setPassword(hashPassword(newPassword));
        userRepository.saveAndFlush(user);
        return user;
    }

    public User updateProfile(String token, String username, String email) {
        User user = getUserByToken(token);
        validateProfileUpdate(user, username, email);
        user.updateProfile(username, email);
        userRepository.saveAndFlush(user);
        return user;
    }

    public User completeOnboarding(String token) {
        User user = getUserByToken(token);
        user.setOnboardingCompleted(true);
        userRepository.saveAndFlush(user);
        return user;
    }

    private void validateProfileUpdate(User existingUser, String username, String email) {
        User userByUsername = userRepository.findByUsername(username);
        if (userByUsername != null && !userByUsername.getId().equals(existingUser.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        User userByEmail = userRepository.findByEmail(email);
        if (userByEmail != null && !userByEmail.getId().equals(existingUser.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
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
        user.setToken(UUID.randomUUID().toString());
        userRepository.saveAndFlush(user);
        return user;

    }

    public List<LeaderboardEntryDTO> getLeaderboard() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .filter(user -> user.getCharacter() != null)
                .sorted((u1, u2) -> {
                    int levelCompare = Integer.compare(u2.getCharacter().getLevel(), u1.getCharacter().getLevel());
                    if (levelCompare != 0) {
                        return levelCompare;
                    }
                    int experienceCompare = Integer.compare(u2.getCharacter().getExperience(),
                            u1.getCharacter().getExperience());
                    if (experienceCompare != 0) {
                        return experienceCompare;
                    }
                    return u1.getUsername().compareTo(u2.getUsername());
                })
                .map(user -> new LeaderboardEntryDTO(user.getUsername(), user.getCharacter().getExperience(),
                        user.getCharacter().getLevel()))
                .toList();
    }
}

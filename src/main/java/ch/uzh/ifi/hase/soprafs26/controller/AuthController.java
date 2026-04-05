package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.LoginPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.LiveService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Auth Controller
 * This class is responsible for handling all REST request that are related to authentication
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

	private final UserService userService;
	private final LiveService presenceService;

	AuthController(UserService userService, LiveService liveService) {
		this.userService = userService;
		this.presenceService = liveService;
	}

	@PostMapping("/register")
	public ResponseEntity<UserGetDTO> register(@Valid @RequestBody UserPostDTO userPostDTO) {
		// convert API user to internal representation
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// create user
		User createdUser = userService.createUser(userInput);

		// convert internal representation of user back to API
		UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
		return ResponseEntity.status(HttpStatus.CREATED)
				.header(HttpHeaders.SET_COOKIE, createAuthCookie(createdUser.getToken()).toString())
				.body(userGetDTO);
	}

	@PostMapping("/login")
	public ResponseEntity<UserGetDTO> login(@Valid @RequestBody LoginPostDTO loginPostDTO) {
		User loggedInUser = userService.login(loginPostDTO.getEmail(), loginPostDTO.getPassword());
		UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(loggedInUser);

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, createAuthCookie(loggedInUser.getToken()).toString())
				.body(userGetDTO);
	}

	@GetMapping("/me")
	public ResponseEntity<UserGetDTO> me(@CookieValue(name = "token", required = false) String tokenCookie,
			@RequestHeader(name = "token", required = false) String tokenHeader,
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
		String token = resolveToken(tokenCookie, tokenHeader, authorizationHeader);
		User user = userService.getUserByToken(token);
		UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
		return ResponseEntity.ok(userGetDTO);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@CookieValue(name = "token", required = false) String tokenCookie,
			@RequestHeader(name = "token", required = false) String tokenHeader,
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
		String token = resolveToken(tokenCookie, tokenHeader, authorizationHeader);
		User loggedOutUser = userService.logout(token);
		presenceService.disconnectUser(loggedOutUser.getId());
		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, clearAuthCookie().toString())
				.build();
	}

	private ResponseCookie createAuthCookie(String token) {
		return ResponseCookie.from("token", token)
				.httpOnly(true)
				.sameSite("Lax")
				.path("/")
				.maxAge(Duration.ofDays(7))
				.build();
	}

	private ResponseCookie clearAuthCookie() {
		return ResponseCookie.from("token", "")
				.httpOnly(true)
				.sameSite("Lax")
				.path("/")
				.maxAge(0)
				.build();
	}

	private String resolveToken(String tokenCookie, String tokenHeader, String authorizationHeader) {
		if (tokenCookie != null && !tokenCookie.isBlank()) {
			return tokenCookie;
		}

		if (tokenHeader != null && !tokenHeader.isBlank()) {
			return tokenHeader;
		}

		if (authorizationHeader != null && !authorizationHeader.isBlank()) {
			if (authorizationHeader.startsWith("Bearer ")) {
				return authorizationHeader.substring(7);
			}
			return authorizationHeader;
		}

		return null;
	}

}

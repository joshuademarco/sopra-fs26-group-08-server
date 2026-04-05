package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.LoginPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

	AuthController(UserService userService) {
		this.userService = userService;
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
				.header("Set-Authorization", createdUser.getToken())
				.body(userGetDTO);
	}

	@PostMapping("/login")
	public ResponseEntity<UserGetDTO> login(@Valid @RequestBody LoginPostDTO loginPostDTO) {
		User loggedInUser = userService.login(loginPostDTO.getEmail(), loginPostDTO.getPassword());
		UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(loggedInUser);

		return ResponseEntity.ok()
				.header("Set-Authorization", loggedInUser.getToken())
				.body(userGetDTO);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader(name = "token", required = false) String tokenHeader,
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
		String token = resolveToken(tokenHeader, authorizationHeader);
		userService.logout(token);
		return ResponseEntity.noContent().build();
	}

	private String resolveToken(String tokenHeader, String authorizationHeader) {
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

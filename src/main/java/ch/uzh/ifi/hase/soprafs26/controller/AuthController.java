package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.LoginPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.LiveService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Auth Controller
 * This class is responsible for handling all REST request that are related to
 * authentication
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
    public ResponseEntity<UserGetDTO> register(@Valid @RequestBody UserPostDTO userPostDTO,
            HttpServletRequest request) {
        // convert API user to internal representation
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // create user
        User createdUser = userService.createUser(userInput, userPostDTO.getType());

        // convert internal representation of user back to API
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, createAuthCookie(createdUser.getToken(), request).toString())
                .body(userGetDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<UserGetDTO> login(@Valid @RequestBody LoginPostDTO loginPostDTO,
            HttpServletRequest request) {
        User loggedInUser = userService.login(loginPostDTO.getEmail(), loginPostDTO.getPassword());
        presenceService.broadcastSnapshot();
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(loggedInUser);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createAuthCookie(loggedInUser.getToken(), request).toString())
                .body(userGetDTO);
    }

    @GetMapping("/me")
    public ResponseEntity<UserGetDTO> me(@CookieValue(name = "token", required = false) String tokenCookie) {
        String token = tokenCookie;
        User user = userService.getUserByToken(token);
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
        return ResponseEntity.ok(userGetDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "token", required = false) String tokenCookie,
            HttpServletRequest request) {
        String token = tokenCookie;
        User loggedOutUser = userService.logout(token);
        presenceService.disconnectUser(loggedOutUser.getId());
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearAuthCookie(request).toString())
                .build();
    }

    private ResponseCookie createAuthCookie(String token, HttpServletRequest request) {
        boolean secure = isSecureRequest(request);
        String sameSite = secure ? "None" : "Lax";

        return ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
    }

    private ResponseCookie clearAuthCookie(HttpServletRequest request) {
        boolean secure = isSecureRequest(request);
        String sameSite = secure ? "None" : "Lax";

        return ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(0)
                .build();
    }

    private boolean isSecureRequest(HttpServletRequest request) {
        if (request.isSecure()) {
            return true;
        }

        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return forwardedProto != null && forwardedProto.equalsIgnoreCase("https");
    }

}

package ch.uzh.ifi.hase.soprafs26.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ChangePasswordDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.LoginPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UpdateProfileDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.LiveService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

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

    @Value("${app.cookie.domain:}")
    private String cookieDomain;

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
        presenceService.broadcastSnapshot();
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

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@CookieValue(name = "token", required = false) String tokenCookie,
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        userService.changePassword(tokenCookie, changePasswordDTO.getCurrentPassword(), changePasswordDTO.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/update-profile")
    public ResponseEntity<UserGetDTO> updateProfile(@CookieValue(name = "token", required = false) String tokenCookie,
            @Valid @RequestBody UpdateProfileDTO updateProfileDTO) {
        User updatedUser = userService.updateProfile(tokenCookie, updateProfileDTO.getUsername(), updateProfileDTO.getEmail());
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(updatedUser);
        return ResponseEntity.ok(userGetDTO);
    }

    @PatchMapping("/onboarding")
    public ResponseEntity<UserGetDTO> completeOnboarding(@CookieValue(name = "token", required = false) String tokenCookie) {
        User updatedUser = userService.completeOnboarding(tokenCookie);
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(updatedUser);
        return ResponseEntity.ok(userGetDTO);
    }

    @GetMapping("/me")
    public ResponseEntity<UserGetDTO> me(@CookieValue(name = "token", required = false) String tokenCookie) {
        User user = userService.getUserByToken(tokenCookie);
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
        return ResponseEntity.ok(userGetDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "token", required = false) String tokenCookie,
            HttpServletRequest request) {
        User loggedOutUser = userService.logout(tokenCookie);
        presenceService.disconnectUser(loggedOutUser.getId());
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearAuthCookie(request).toString())
                .build();
    }

    private ResponseCookie createAuthCookie(String token, HttpServletRequest request) {
        boolean secure = isSecureRequest(request);
        String sameSite = secure ? "None" : "Lax";

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ofDays(7));
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
        return builder.build();
    }

    private ResponseCookie clearAuthCookie(HttpServletRequest request) {
        boolean secure = isSecureRequest(request);
        String sameSite = secure ? "None" : "Lax";

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(0);
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
        return builder.build();
    }

    private boolean isSecureRequest(HttpServletRequest request) {
        if (request.isSecure()) {
            return true;
        }

        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return forwardedProto != null && forwardedProto.equalsIgnoreCase("https");
    }

}

package ch.uzh.ifi.hase.soprafs26.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ChangePasswordDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.LoginPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UpdateProfileDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.LiveService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private LiveService liveService;

    @InjectMocks
    private AuthController authController;

    private User user;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setId(1L);
        user.setEmail("testemail@uzh.ch");
        user.setPassword("password123");
        user.setUsername("testUsername");
        user.setToken("testToken123");
    }

    @Test
    public void register_validInput_userCreatedAndCookieSet() {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setEmail("testemail@uzh.ch");
        userPostDTO.setPassword("password123");
        userPostDTO.setType("USER");

        given(userService.createUser(any(User.class), eq("USER"))).willReturn(user);
        doNothing().when(liveService).broadcastSnapshot();
        
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.isSecure()).thenReturn(false);

        ResponseEntity<UserGetDTO> response = authController.register(userPostDTO, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(user.getId(), response.getBody().getId());
        assertEquals(user.getUsername(), response.getBody().getUsername());
        assertEquals(user.getEmail(), response.getBody().getEmail());
        
        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader);
        assertTrue(setCookieHeader.contains("token=testToken123"));
    }

    @Test
    public void login_validCredentials_userLoggedInAndCookieSet() {
        LoginPostDTO loginPostDTO = new LoginPostDTO();
        loginPostDTO.setEmail("testemail@uzh.ch");
        loginPostDTO.setPassword("password123");

        given(userService.login("testemail@uzh.ch", "password123")).willReturn(user);
        doNothing().when(liveService).broadcastSnapshot();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.isSecure()).thenReturn(false);

        ResponseEntity<UserGetDTO> response = authController.login(loginPostDTO, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(user.getId(), response.getBody().getId());
        assertEquals(user.getUsername(), response.getBody().getUsername());
        assertEquals(user.getEmail(), response.getBody().getEmail());

        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader);
        assertTrue(setCookieHeader.contains("token=testToken123"));
    }

    @Test
    public void register_duplicateUsername_throwsException() {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("duplicateUser");
        userPostDTO.setEmail("newemail@uzh.ch");
        userPostDTO.setPassword("password123");
        userPostDTO.setType("USER");

        HttpServletRequest request = mock(HttpServletRequest.class);

        given(userService.createUser(any(User.class), eq("USER")))
                .willThrow(new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authController.register(userPostDTO, request);
        });
        
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Username already exists", exception.getReason());
    }

    @Test
    public void login_invalidCredentials_throwsException() {
        LoginPostDTO loginPostDTO = new LoginPostDTO();
        loginPostDTO.setEmail("wrongemail@uzh.ch");
        loginPostDTO.setPassword("wrongpassword");

        HttpServletRequest request = mock(HttpServletRequest.class);

        given(userService.login("wrongemail@uzh.ch", "wrongpassword"))
                .willThrow(new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authController.login(loginPostDTO, request);
        });
        
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid credentials", exception.getReason());
    }
    @Test
    public void changePassword_validInput_passwordChanged() {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("oldPassword123");
        changePasswordDTO.setNewPassword("newPassword123");

        given(userService.changePassword("testToken123", "oldPassword123", "newPassword123")).willReturn(user);

        ResponseEntity<Void> response = authController.changePassword("testToken123", changePasswordDTO);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).changePassword("testToken123", "oldPassword123", "newPassword123");
    }

    @Test
    public void updateProfile_validInput_profileUpdated() {
        UpdateProfileDTO updateProfileDTO = new UpdateProfileDTO();
        updateProfileDTO.setUsername("newUsername");
        updateProfileDTO.setEmail("newemail@uzh.ch");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("newUsername");
        updatedUser.setEmail("newemail@uzh.ch");
        updatedUser.setToken("testToken123");

        given(userService.updateProfile("testToken123", "newUsername", "newemail@uzh.ch")).willReturn(updatedUser);

        ResponseEntity<UserGetDTO> response = authController.updateProfile("testToken123", updateProfileDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(updatedUser.getId(), response.getBody().getId());
        assertEquals(updatedUser.getUsername(), response.getBody().getUsername());
        assertEquals(updatedUser.getEmail(), response.getBody().getEmail());
    }

    @Test
    public void me_validToken_returnsUser() {
        given(userService.getUserByToken("testToken123")).willReturn(user);

        ResponseEntity<UserGetDTO> response = authController.me("testToken123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(user.getId(), response.getBody().getId());
        assertEquals(user.getUsername(), response.getBody().getUsername());
        assertEquals(user.getEmail(), response.getBody().getEmail());
    }

    @Test
    public void logout_validToken_userLoggedOutAndCookieCleared() {
        given(userService.logout("testToken123")).willReturn(user);
        doNothing().when(liveService).disconnectUser(user.getId());

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.isSecure()).thenReturn(false);

        ResponseEntity<Void> response = authController.logout("testToken123", request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        
        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader);
        assertTrue(setCookieHeader.contains("token="));
        assertTrue(setCookieHeader.contains("Max-Age=0"));
    }
}
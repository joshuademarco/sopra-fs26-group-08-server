package ch.uzh.ifi.hase.soprafs26.controller;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CalendarConnectGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CalendarEventGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.FreeSlotGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.FreeSlotRequestDTO;
import ch.uzh.ifi.hase.soprafs26.service.CalendarService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class CalendarController {

    private final CalendarService calendarService;
    private final UserService userService;

    CalendarController(CalendarService calendarService, UserService userService) {
        this.calendarService = calendarService;
        this.userService = userService;
    }

    /**
     * Returns the Google OAuth2 URL the frontend should redirect to
     */
    @GetMapping("/users/{userId}/calendar/connect")
    @ResponseStatus(HttpStatus.OK)
    public CalendarConnectGetDTO getConnectUrl(@PathVariable Long userId,
            @CookieValue(name = "token", required = true) String token) {

        User requestingUser = userService.getUserByToken(token);
        verifyOwnership(requestingUser.getId(), userId);

        CalendarConnectGetDTO dto = new CalendarConnectGetDTO();
        dto.setAuthUrl(calendarService.getConnectUrl(userId));
        dto.setConnected(calendarService.isConnected(userId));
        return dto;
    }

    /**
     * OAuth2 redirect endpoint — for Google callbacks
     */
    @GetMapping("/calendar/callback")
    public void handleOAuthCallback(@RequestParam String code, @RequestParam String state, HttpServletResponse response)
            throws IOException {

        Long userId = Long.parseLong(state);
        calendarService.handleOAuthCallback(code, userId);
        response.sendRedirect(calendarService.getFrontendRedirectUri() + "?calendarConnected=true");
    }

    /**
     * Returns the users next 20 upcoming Calendar Events.
     */
    @GetMapping("/users/{userId}/calendar/events")
    @ResponseStatus(HttpStatus.OK)
    public List<CalendarEventGetDTO> getEvents(@PathVariable Long userId,
            @CookieValue(name = "token", required = true) String token) {

        User requestingUser = userService.getUserByToken(token);
        verifyOwnership(requestingUser.getId(), userId);

        return calendarService.getEvents(userId);
    }

    /**
     * Finds time slots within the parameter window where all given users are free
     */
    @PostMapping("/users/{userId}/calendar/free-slots")
    @ResponseStatus(HttpStatus.OK)
    public List<FreeSlotGetDTO> findFreeSlots(@PathVariable Long userId, @RequestBody FreeSlotRequestDTO requestDTO,
            @CookieValue(name = "token", required = true) String token) {

        User requestingUser = userService.getUserByToken(token);
        verifyOwnership(requestingUser.getId(), userId);

        Instant from = Instant.parse(requestDTO.getFrom());
        Instant to = Instant.parse(requestDTO.getTo());

        return calendarService.findFreeSlots(requestDTO.getUserIds(), from, to);
    }

    /**
     * Disconnects the user's Calendar by removing tokens.
     */
    @DeleteMapping("/users/{userId}/calendar/disconnect")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disconnect(@PathVariable Long userId, @CookieValue(name = "token", required = true) String token) {

        User requestingUser = userService.getUserByToken(token);
        verifyOwnership(requestingUser.getId(), userId);

        calendarService.disconnect(userId);
    }

    private void verifyOwnership(Long requestingUserId, Long targetUserId) {
        if (!requestingUserId.equals(targetUserId)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only access your own calendar");
        }
    }
}

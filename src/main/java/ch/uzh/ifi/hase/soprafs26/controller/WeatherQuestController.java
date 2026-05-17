package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.WeatherQuestGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.service.WeatherQuestService;

@RestController
public class WeatherQuestController {
    private final WeatherQuestService weatherQuestService;
    private final UserService userService;

    WeatherQuestController(WeatherQuestService weatherQuestService, UserService userService) {
        this.weatherQuestService = weatherQuestService;
        this.userService = userService;
    }

    @GetMapping("/users/{userId}/weather-quest")
    @ResponseStatus(HttpStatus.OK)
    public WeatherQuestGetDTO getWeatherQuestDTO(@PathVariable Long userId, @CookieValue(name = "token", required = true) String token) {
        User requestingUser = userService.getUserByToken(token);
        verifyOwnership(requestingUser.getId(), userId);
        return weatherQuestService.getWeatherQuest(userId);
    }

    private void verifyOwnership(Long requestingUserId, Long targetUserId) {
        if (!requestingUserId.equals(targetUserId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You can only access your own weather quest");
        }
    }
}

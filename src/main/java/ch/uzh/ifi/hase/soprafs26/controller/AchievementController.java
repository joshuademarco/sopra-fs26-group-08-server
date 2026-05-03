package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.rest.dto.AchievementGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.AchievementService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

@RestController
public class AchievementController {

    private final AchievementService achievementService;
    private final UserService userService;

    AchievementController(AchievementService achievementService, UserService userService) {
        this.achievementService = achievementService;
        this.userService = userService;
    }

    @GetMapping("/users/{userId}/achievements")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<AchievementGetDTO> getAchievements(
            @PathVariable Long userId,
            @CookieValue(name = "token", required = true) String token) {

        userService.getUserByToken(token);

        return achievementService.getAchievementsForUser(userId).stream()
                .map(DTOMapper.INSTANCE::convertEntityToAchievementGetDTO)
                .collect(Collectors.toList());
    }
}

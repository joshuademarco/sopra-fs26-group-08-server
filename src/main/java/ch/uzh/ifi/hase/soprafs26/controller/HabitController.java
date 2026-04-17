package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.stream.Collectors;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Habit;
import ch.uzh.ifi.hase.soprafs26.rest.dto.HabitGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.HabitPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.HabitService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import jakarta.validation.Valid;

@RestController
public class HabitController {
    private final HabitService habitService;
    private final UserService userService;

    HabitController(HabitService habitService, UserService userService) {
        this.habitService = habitService;
        this.userService = userService;
    }

    // ============================== weather api ==============================
    @GetMapping("/weather")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public int getWeather() throws Exception {
        int weatherCode = habitService.getWeatherCode();
        return weatherCode;
    }

    // ============================== habits ==============================
    @GetMapping("/users/{userId}/habits")
    @ResponseStatus(HttpStatus.OK)
    public List<HabitGetDTO> getHabits(@PathVariable Long userId,
            @CookieValue(name = "token", required = true) String token) {
        User requestingUser = userService.getUserByToken(token);
        verifyOwnership(requestingUser.getId(), userId);

        List<Habit> habits = habitService.getHabitsForUser(userId);

        return habits.stream()
                .map(DTOMapper.INSTANCE::convertEntityToHabitGetDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/users/{userId}/habits")
    @ResponseStatus(HttpStatus.CREATED)
    public HabitGetDTO createHabit(@PathVariable Long userId, @Valid @RequestBody HabitPostDTO habitPostDTO,
            @CookieValue(name = "token", required = true) String token) {
        User requestingUser = userService.getUserByToken(token);
        verifyOwnership(requestingUser.getId(), userId);

        Habit habit = DTOMapper.INSTANCE.convertHabitPostDTOtoEntity(habitPostDTO);
        Habit createdHabit = habitService.createHabit(userId, habit);

        return DTOMapper.INSTANCE.convertEntityToHabitGetDTO(createdHabit);
    }

    @PutMapping("/users/{userId}/habits/{habitId}/complete")
    @ResponseStatus(HttpStatus.OK)
    public HabitGetDTO completeHabit(@PathVariable Long userId, @PathVariable Long habitId,
            @CookieValue(name = "token", required = true) String token) {
        User requestingUser = userService.getUserByToken(token);
        verifyOwnership(requestingUser.getId(), userId);

        Habit habit = habitService.completeHabit(habitId, requestingUser.getId());
        return DTOMapper.INSTANCE.convertEntityToHabitGetDTO(habit);
    }

    @DeleteMapping("/users/{userId}/habits/{habitId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHabit(@PathVariable Long userId, @PathVariable Long habitId,
            @CookieValue(name = "token", required = true) String token) {
        User requestingUser = userService.getUserByToken(token);
        verifyOwnership(requestingUser.getId(), userId);

        habitService.deleteHabit(habitId, requestingUser.getId());
    }

    // helper ensuring logged in user is same as userId in url
    private void verifyOwnership(Long requestingUserId, Long targetUserId) {
        if (!requestingUserId.equals(targetUserId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You can only access your own habits");
        }
    }
}

package ch.uzh.ifi.hase.soprafs26.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Habit;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.HabitRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

@Service
@Transactional // will update habits and XP tables once XP is implemented
public class HabitService {
    private final WeatherService weatherService;
    private final HabitRepository habitRepository;
    private final UserRepository userRepository;

    public HabitService(WeatherService weatherService, HabitRepository habitRepository, UserRepository userRepository) {
        this.weatherService = weatherService;
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
    }

    // ============================== weather api ==============================
    public int getWeatherCode() throws Exception {
        String json = weatherService.getWeather();
        return weatherService.parseWeatherData(json);
    }

    // ============================== habits ==============================
    
    public List<Habit> getHabitsForUser(Long userId) {
        getUserOrThrow(userId);
        return habitRepository.findByUserId(userId);
    }

    public Habit createHabit(Long userId, Habit habit) {
        User user = getUserOrThrow(userId);

        habit.setUser(user);
        habit.setDueAt(calculateDueDate(habit));

        return habitRepository.save(habit);
    }

    public Habit completeHabit(Long habitId, Long userId) {
        Habit habit = getHabitOrThrow(habitId);

        // verify habit belongs to user
        if (!habit.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only complete your own habits");
        }

        if (habit.getCompleted()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Habit already completed");
        }

        habit.complete();
        habit.setStreak(habit.getStreak() + 1);
        habit.setLastCompletedAt(Instant.now());

        // TODO: XP and stat but implement it in character

        habitRepository.save(habit);
        return habit;
    }

    public void deleteHabit(Long habitId, Long userId) {
        Habit habit = getHabitOrThrow(habitId);

        if (!habit.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only delete your own habits");
        }

        habitRepository.delete(habit);
    }

    // helper methods
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    private Habit getHabitOrThrow(Long habitId) {
        return habitRepository.findById(habitId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Habit not found"));
    }

    private Instant calculateDueDate(Habit habit) {
        Instant now = Instant.now();
        switch (habit.getFrequency()) {
            case DAILY:
                return now.plus(1, ChronoUnit.DAYS);
            case WEEKLY:
                return now.plus(7, ChronoUnit.DAYS);
            case MONTHLY:
                return now.plus(30, ChronoUnit.DAYS);
            default:
                return now.plus(1, ChronoUnit.DAYS);
        }
    }
}

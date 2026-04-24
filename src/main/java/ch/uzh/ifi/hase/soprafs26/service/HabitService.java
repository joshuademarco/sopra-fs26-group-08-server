package ch.uzh.ifi.hase.soprafs26.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import ch.uzh.ifi.hase.soprafs26.entity.Habit;
import ch.uzh.ifi.hase.soprafs26.entity.HabitCompletionEvent;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.HabitCompletionEventRepository;
import ch.uzh.ifi.hase.soprafs26.repository.HabitRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

@Service
@Transactional // ensures DB data integrity for habit completion & awarding XP
               // -> rolls back if anyhting fails
public class HabitService {
    private final Logger log = LoggerFactory.getLogger(HabitService.class);
    
    private final WeatherService weatherService;
    private final HabitRepository habitRepository;
    private final UserRepository userRepository;
    private final CharacterService characterService;
    private final HabitCompletionEventRepository completionEventRepository;

    public HabitService(WeatherService weatherService,
            HabitRepository habitRepository,
            UserRepository userRepository,
            CharacterService characterService,
            HabitCompletionEventRepository completionEventRepository) {
        this.weatherService = weatherService;
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
        this.characterService = characterService;
        this.completionEventRepository = completionEventRepository;
    }

    // ============================== weather ==============================
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
        habitRepository.save(habit);

        // based on habit weight (difficulty)
        int baseXp = characterService.calculateBaseXp(habit.getWeight());

        double weatherMultiplier = getWeatherMultiplierSafely(habit.getCategory());
        int weatherCode = getWeatherCodeSafely();

        int finalXp = characterService.awardXp(
                userId, habit.getCategory(), baseXp, weatherMultiplier);

        // added this for now so e.g. we could later show history of all completions
        recordCompletionEvent(habit, getUserOrThrow(userId),
                baseXp, finalXp, weatherMultiplier, weatherCode);

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

    private void recordCompletionEvent(Habit habit, User user,
            int baseXp, int multipliedXp,
            double weatherMultiplier, int weatherCode) {
        HabitCompletionEvent event = new HabitCompletionEvent();
        event.setHabit(habit);
        event.setUser(user);
        event.setBaseXp(baseXp);
        event.setMultipliedXp(multipliedXp);
        event.setWeatherMultiplier(weatherMultiplier);
        event.setWeatherCode(weatherCode);
        event.setCompletedAt(Instant.now());
        completionEventRepository.save(event);
        log.debug("Recorded completion: {} XP for habit '{}'", multipliedXp, habit.getTitle());
    }


    private double getWeatherMultiplierSafely(HabitCategory category) {
        try {
            String json = weatherService.getWeather();
            int code = weatherService.parseWeatherData(json);
            String categoryStr = category.name().toLowerCase();
            return weatherService.getMultiplier(code, categoryStr);
        } catch (Exception e) { // safety mechanism so broken API never blocks habit completion
            log.warn("Weather API unavailable, using 1.0x multiplier: {}", e.getMessage());
            return 1.0;
        }
    }

    private int getWeatherCodeSafely() {
        try {
            String json = weatherService.getWeather();
            return weatherService.parseWeatherData(json);
        } catch (Exception e) {
            return 0; // safety default (0 = clear sky) if API broken
        }
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
}

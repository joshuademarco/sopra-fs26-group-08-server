package ch.uzh.ifi.hase.soprafs26.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@Transactional
public class HabitService {
    private final Logger log = LoggerFactory.getLogger(HabitService.class);

    private final WeatherService weatherService;
    private final HabitRepository habitRepository;
    private final UserRepository userRepository;
    private final CharacterService characterService;
    private final HabitCompletionEventRepository completionEventRepository;
    private final AchievementService achievementService;

    public HabitService(WeatherService weatherService,
            HabitRepository habitRepository,
            UserRepository userRepository,
            CharacterService characterService,
            HabitCompletionEventRepository completionEventRepository,
            AchievementService achievementService) {
        this.weatherService = weatherService;
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
        this.characterService = characterService;
        this.completionEventRepository = completionEventRepository;
        this.achievementService = achievementService;
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

    public Map<LocalDate, Long> getHabitHeatmap(Long userId) {
        getUserOrThrow(userId);
        List<HabitCompletionEvent> events = completionEventRepository.findByUserId(userId);
        Map<LocalDate, Long> heatmap = new HashMap<>();
        for (HabitCompletionEvent event : events) {
            LocalDate date = event.getCompletedAt().atZone(ZoneOffset.UTC).toLocalDate();
            if (heatmap.containsKey(date)) {
                heatmap.put(date, heatmap.get(date) + 1);
            } else {
                heatmap.put(date, 1L);
            }
        }
        return heatmap;
    }

    public Habit createHabit(Long userId, Habit habit) {
        User user = getUserOrThrow(userId);

        habit.setUser(user);
        habit.setDueAt(calculateDueDate(habit));

        return habitRepository.save(habit);
    }

    public Habit completeHabit(Long habitId, Long userId) {
        Habit habit = getHabitOrThrow(habitId);

        if (!habit.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only complete your own habits");
        }

        if (habit.getCompleted()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Habit already completed");
        }

        habit.complete();
        if (isStreakContinued(habit)) {
            habit.setStreak(habit.getStreak() + 1);
        } else {
            habit.setStreak(1); // reset streak
        }
        habit.setLastCompletedAt(Instant.now());
        habitRepository.save(habit);

        if (habit.getPositive()) {
            achievementService.checkHabitAchievements(userId, habit.getStreak());
            // positive habit -> award XP + update stat
            int baseXp = characterService.calculateBaseXp(habit.getWeight());
            double weatherMultiplier = getWeatherMultiplierSafely(habit.getCategory());
            int weatherCode = getWeatherCodeSafely();
            int finalXp = characterService.awardXp(userId, habit.getCategory(), baseXp, weatherMultiplier);

            // record positive habit completion with XP details
            recordCompletionEvent(habit, getUserOrThrow(userId), baseXp, finalXp, weatherMultiplier, weatherCode);
        } else {
            // negative habit -> health penalty only, no XP + no stat increase
            characterService.applyNegativeHabitPenalty(userId, habit.getWeight());

            // record event with zero XP to show in history
            recordCompletionEvent(habit, getUserOrThrow(userId),
                    0, 0, 1.0, 0);
        }

        return habit;
    }

    private boolean isStreakContinued(Habit habit) {
        if (habit.getLastCompletedAt() == null) {
            return false; // first ever completion
        }

        Instant last = habit.getLastCompletedAt();
        Instant now = Instant.now();

        long maxGapSeconds = switch (habit.getFrequency()) {
            case DAILY -> 2L * 24 * 3600; // 1 day + 1 buffer day
            case WEEKLY -> 8L * 24 * 3600; // 7 days + 1 buffer day
            case MONTHLY -> 32L * 24 * 3600; // 31 days + 1 buffer day
        };

        return now.getEpochSecond() - last.getEpochSecond() <= maxGapSeconds;
    }

    public void resetOverdueHabits() {
        Instant now = Instant.now();
        List<Habit> overdueHabits = habitRepository.findByCompletedFalseAndDueAtBefore(now);

        for (Habit habit : overdueHabits) {
            if (habit.getPositive()) {
                // positive habit missed then health penalty
                characterService.applyNegativeHabitPenalty(habit.getUser().getId(), habit.getWeight());
                habit.setPenaltyApplied(true); // UI signal: show warning on this habit card (#60 + #7)
            }
            habit.setStreak(0);
            habit.setDueAt(calculateDueDate(habit));
            habitRepository.save(habit);
            log.debug("Streak reset for overdue habit '{}', penalty={}", habit.getTitle(), habit.getPenaltyApplied());
        }

        // reset the completed flag for habits that are past due but were completed
        List<Habit> completedPastDue = habitRepository.findByCompletedTrueAndDueAtBefore(now);
        for (Habit habit : completedPastDue) {
            habit.setCompleted(false);
            habit.setCompletedAt(null);
            habit.setPenaltyApplied(false);
            habitRepository.save(habit);
            log.debug("Reset completed habit '{}' for new period", habit.getTitle());
        }
    }

    public void deleteHabit(Long habitId, Long userId) {
        Habit habit = getHabitOrThrow(habitId);

        if (!habit.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only delete your own habits");
        }
        completionEventRepository.deleteAll(completionEventRepository.findByHabitId(habitId));
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

    public int getWeatherCodeSafely() {
        try {
            String json = weatherService.getWeather();
            return weatherService.parseWeatherData(json);
        } catch (Exception e) {
            return 0; // safety default (0 = clear sky) if API broken
        }
    }

    public double getMultiplierForCategory(int weatherCode, HabitCategory category) {
        return weatherService.getMultiplier(weatherCode, category.name().toLowerCase());
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

package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import ch.uzh.ifi.hase.soprafs26.constant.HabitFrequency;
import ch.uzh.ifi.hase.soprafs26.entity.Habit;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.HabitCompletionEventRepository;
import ch.uzh.ifi.hase.soprafs26.repository.HabitRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class HabitServiceTest {

    @Mock
    private WeatherService weatherService;
    @Mock
    private HabitRepository habitRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CharacterService characterService;
    @Mock
    private HabitCompletionEventRepository completionEventRepository;

    @InjectMocks
    private HabitService habitService;

    private User testUser;
    private Habit testHabit;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@test.com");
        testUser.setToken("test-token");
        testUser.setStatus(ch.uzh.ifi.hase.soprafs26.constant.UserStatus.ONLINE);

        testHabit = new Habit();
        testHabit.setId(1L);
        testHabit.setTitle("Morning Run");
        testHabit.setCategory(HabitCategory.PHYSICAL);
        testHabit.setFrequency(HabitFrequency.DAILY);
        testHabit.setPositive(true);
        testHabit.setWeight(1);
        testHabit.setUser(testUser);
        testHabit.setStreak(0);
        testHabit.setCompleted(false);
    }

    // ---------------tests for user getting habits---------------
    @Test
    public void getHabitsForUser_validUser_returnsHabits() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(habitRepository.findByUserId(1L)).thenReturn(List.of(testHabit));

        List<Habit> result = habitService.getHabitsForUser(1L);

        assertEquals(1, result.size());
        assertEquals("Morning Run", result.get(0).getTitle());
    }

    @Test
    public void getHabitsForUser_userNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> habitService.getHabitsForUser(99L));
    }

    // ---------------tests for user creating habit---------------
    @Test
    public void createHabit_validInput_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(habitRepository.save(any())).thenReturn(testHabit);

        Habit result = habitService.createHabit(1L, testHabit);

        assertNotNull(result);
        assertEquals("Morning Run", result.getTitle());
        // user is linked to habit
        assertEquals(testUser, result.getUser());
        // dueAt should be set based on frequency
        assertNotNull(result.getDueAt());
        verify(habitRepository, times(1)).save(any());
    }

    @Test
    public void createHabit_userNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> habitService.createHabit(99L, testHabit));
    }

    @Test
    public void createHabit_dailyFrequency_dueAtIsOneDayFromNow() {
        testHabit.setFrequency(HabitFrequency.DAILY);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(habitRepository.save(any())).thenReturn(testHabit);

        habitService.createHabit(1L, testHabit);

        // dueAt should be approximately 1 day from now
        assertNotNull(testHabit.getDueAt());
        long secondsUntilDue = testHabit.getDueAt().getEpochSecond()
                - java.time.Instant.now().getEpochSecond();
        // should be between 23 and 25 hours
        assertTrue(secondsUntilDue > 23 * 3600 && secondsUntilDue < 25 * 3600);
    }

    // ---------------tests for user completing habit---------------
    @Test
    public void completeHabit_validOwner_marksComplete() throws Exception {
        when(habitRepository.findById(1L)).thenReturn(Optional.of(testHabit));
        when(habitRepository.save(any())).thenReturn(testHabit);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(characterService.calculateBaseXp(1)).thenReturn(10);
        when(weatherService.getWeather()).thenReturn("{\"current\":{\"weather_code\":0,\"temperature_2m\":20}}");
        when(weatherService.parseWeatherData(any())).thenReturn(0);
        when(weatherService.getMultiplier(anyInt(), any())).thenReturn(1.0);
        when(characterService.awardXp(any(), any(), anyInt(), anyDouble())).thenReturn(10);
        when(completionEventRepository.save(any())).thenReturn(null);

        Habit result = habitService.completeHabit(1L, 1L);

        assertTrue(result.getCompleted());
        assertEquals(1, result.getStreak());
        assertNotNull(result.getLastCompletedAt());
    }

    @Test
    public void completeHabit_alreadyCompleted_throwsConflict() {
        testHabit.setCompleted(true);
        when(habitRepository.findById(1L)).thenReturn(Optional.of(testHabit));

        assertThrows(ResponseStatusException.class, () -> habitService.completeHabit(1L, 1L));
    }

    @Test
    public void completeHabit_wrongUser_throwsForbidden() {
        when(habitRepository.findById(1L)).thenReturn(Optional.of(testHabit));

        // user with id 2 tries to complete habit of user with id 1
        assertThrows(ResponseStatusException.class, () -> habitService.completeHabit(1L, 2L));
    }

    @Test
    public void completeHabit_habitNotFound_throwsNotFound() {
        when(habitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> habitService.completeHabit(99L, 1L));
    }

    @Test
    public void completeHabit_weatherApiFails_stillCompletes() throws Exception {
        // weatherAPI throws exception —> habit should still complete with just 1.0
        // multiplier
        when(habitRepository.findById(1L)).thenReturn(Optional.of(testHabit));
        when(habitRepository.save(any())).thenReturn(testHabit);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(characterService.calculateBaseXp(1)).thenReturn(10);
        when(weatherService.getWeather()).thenThrow(new RuntimeException("API is down"));
        when(characterService.awardXp(any(), any(), anyInt(), anyDouble())).thenReturn(10);
        when(completionEventRepository.save(any())).thenReturn(null);

        // even if weather API fails -> habit should still be marked as completed
        Habit result = habitService.completeHabit(1L, 1L);

        assertTrue(result.getCompleted());
    }

    @Test
    public void completeHabit_awardsXpToCharacter() throws Exception {
        when(habitRepository.findById(1L)).thenReturn(Optional.of(testHabit));
        when(habitRepository.save(any())).thenReturn(testHabit);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(characterService.calculateBaseXp(1)).thenReturn(10);
        when(weatherService.getWeather()).thenThrow(new RuntimeException("skip weather"));
        when(characterService.awardXp(any(), any(), anyInt(), anyDouble())).thenReturn(10);
        when(completionEventRepository.save(any())).thenReturn(null);

        habitService.completeHabit(1L, 1L);

        verify(characterService, times(1)).awardXp(
                eq(1L), eq(HabitCategory.PHYSICAL), eq(10), anyDouble());
    }

    // ---------------tests for user deleting habit---------------
    @Test
    public void deleteHabit_validOwner_deletesHabit() {
        when(habitRepository.findById(1L)).thenReturn(Optional.of(testHabit));

        habitService.deleteHabit(1L, 1L);

        verify(habitRepository, times(1)).delete(testHabit);
    }

    @Test
    public void deleteHabit_wrongUser_throwsForbidden() {
        when(habitRepository.findById(1L)).thenReturn(Optional.of(testHabit));

        assertThrows(ResponseStatusException.class, () -> habitService.deleteHabit(1L, 2L));
    }

    @Test
    public void deleteHabit_habitNotFound_throwsNotFound() {
        when(habitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> habitService.deleteHabit(99L, 1L));
    }
}
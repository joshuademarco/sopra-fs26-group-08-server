package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ch.uzh.ifi.hase.soprafs26.entity.HabitCompletionEvent;
import ch.uzh.ifi.hase.soprafs26.repository.HabitCompletionEventRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.WeatherQuestGetDTO;

public class WeatherQuestServiceTest {
    @InjectMocks
    private WeatherQuestService weatherQuestService;

    @Mock
    private WeatherService weatherService;

    @Mock
    private HabitCompletionEventRepository habitCompletionEventRepository;

    private Long userId = 1L;

    @BeforeEach
    public void setup() throws Exception{
        MockitoAnnotations.openMocks(this);

        when(weatherService.getDailyWeather()).thenReturn("{\"daily\":{\"weather_code\":[1]}}");
        when(weatherService.parseDailyWeatherData(any())).thenReturn(1);    
        when(habitCompletionEventRepository
                .findByUserIdAndHabitCategoryAndCompletedAtBetween(any(), any(), any(), any()))
                .thenReturn(List.of());
    }

    @Test
    public void getWeatherQuest_sunny_returnsClearQuest() {
        WeatherQuestGetDTO dto = weatherQuestService.getWeatherQuest(userId);

        assertEquals("CLEAR", dto.getWeatherCondition());
    }

    @Test
    public void getWeatherQuest_rainy_returnsRainQuest() {
        when(weatherService.parseDailyWeatherData(any())).thenReturn(55);
        
        WeatherQuestGetDTO dto = weatherQuestService.getWeatherQuest(userId);

        assertEquals("RAIN", dto.getWeatherCondition());
    }

    @Test
    public void getWeatherQuest_weatherApiFails_fallsBackToClear() throws Exception {
        when(weatherService.getDailyWeather()).thenThrow(new RuntimeException("Test"));

        WeatherQuestGetDTO dto = weatherQuestService.getWeatherQuest(userId);

        assertEquals("CLEAR", dto.getWeatherCondition());

    }

    @Test
    public void getWeatherQuest_noCompletionsToday_countIsZero() {
        WeatherQuestGetDTO dto = weatherQuestService.getWeatherQuest(userId);

        assertEquals(0, dto.getCompletedCount());
        assertEquals(false, dto.isCompleted());
    }

    @Test
    public void getWeatherQuest_enoughCompletions_markedCompleted() {
        HabitCompletionEvent habit1 = mock(HabitCompletionEvent.class);
        HabitCompletionEvent habit2 = mock(HabitCompletionEvent.class);
        HabitCompletionEvent habit3 = mock(HabitCompletionEvent.class);
        when(habitCompletionEventRepository.findByUserIdAndHabitCategoryAndCompletedAtBetween(any(), any(), any(), any())).thenReturn(List.of(habit1, habit2, habit3));
        
        WeatherQuestGetDTO dto = weatherQuestService.getWeatherQuest(userId);

        assertEquals(true, dto.isCompleted());
    }

    @Test
    public void getWeatherQuest_SameDay_returnsSameTemplate() {
        WeatherQuestGetDTO dto1 = weatherQuestService.getWeatherQuest(userId);
        WeatherQuestGetDTO dto2 = weatherQuestService.getWeatherQuest(userId);

        assertEquals(dto1.getQuestTitle(), dto2.getQuestTitle());
    }
}

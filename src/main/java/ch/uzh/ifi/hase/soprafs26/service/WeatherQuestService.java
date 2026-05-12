package ch.uzh.ifi.hase.soprafs26.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import ch.uzh.ifi.hase.soprafs26.repository.HabitCompletionEventRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.WeatherQuestGetDTO;

@Service
public class WeatherQuestService {
    private final WeatherService weatherService;
    private final HabitCompletionEventRepository habitCompletionEventRepository;

    public WeatherQuestService(HabitCompletionEventRepository habitCompletionEventRepository,
            WeatherService weatherService) {
        this.habitCompletionEventRepository = habitCompletionEventRepository;
        this.weatherService = weatherService;
    }

    private record QuestTemplate(
            String weatherCondition, String weatherLabel,
            String questTitle, HabitCategory targetCategory,
            int targetCount, String bonusStat, double bonusMultiplier) {
    }

    private final Map<String, List<QuestTemplate>> questPool = Map.of(
            "clear", List.of(
                    new QuestTemplate("CLEAR", "Sunny", "Sun's Out: Complete 3 physical habits",
                            HabitCategory.PHYSICAL, 3, "Strength", 1.0),
                    new QuestTemplate("CLEAR", "Sunny", "Clear Mind, Sharp Focus: Complete 3 cognitive habits",
                            HabitCategory.COGNITIVE, 3, "Intelligence", 1.8)),
            "cloudy", List.of(
                    new QuestTemplate("CLOUDY", "Cloudy", "Overcast Grind: Complete 3 cognitive habits",
                            HabitCategory.COGNITIVE, 3, "Intelligence", 1.6),
                    new QuestTemplate("CLOUDY", "Cloudy", "Grey Day, Warm Heart: Complete 3 emotional habits",
                            HabitCategory.EMOTIONAL, 3, "Resilience", 1.2)),
            "rain", List.of(
                    new QuestTemplate("RAIN", "Rainy", "Rainy Day Grind: Complete 3 cognitive habits",
                            HabitCategory.COGNITIVE, 3, "Intelligence", 1.4),
                    new QuestTemplate("RAIN", "Rainy", "Storm the Soul: Complete 3 emotional habits",
                            HabitCategory.EMOTIONAL, 3, "Resilience", 1.6)),
            "snow", List.of(
                    new QuestTemplate("SNOW", "Snowy", "Snow Day Hustle: Complete 2 physical habits",
                            HabitCategory.PHYSICAL, 2, "Strength", 1.8),
                    new QuestTemplate("SNOW", "Snowy", "Frozen Resolve: Complete 2 emotional habits",
                            HabitCategory.EMOTIONAL, 2, "Resilience", 1.8)),
            "storm", List.of(
                    new QuestTemplate("STORM", "Stormy", "Eye of the Storm: Complete 2 physical habits",
                            HabitCategory.PHYSICAL, 2, "Strength", 2.0),
                    new QuestTemplate("STORM", "Stormy", "Storm Endurance: Complete 2 emotional habits",
                            HabitCategory.EMOTIONAL, 2, "Resilience", 1.0)));

    public WeatherQuestGetDTO getWeatherQuest(Long userId) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Instant start = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        String weatherString;
        try {
            weatherString = getWeatherString(weatherService.parseWeatherData(weatherService.getWeather()));
        } catch (Exception e) {
            weatherString = "clear";
        }
        List<QuestTemplate> options = questPool.get(weatherString);
        QuestTemplate t = options.get((int)(new Random(userId + today.toEpochDay()).nextInt(2)));
        int count = habitCompletionEventRepository.findByUserIdAndHabitCategoryAndCompletedAtBetween(userId, t.targetCategory(), start, end).size();

        WeatherQuestGetDTO dto = new WeatherQuestGetDTO();
        dto.setWeatherCondition(t.weatherCondition());
        dto.setWeatherLabel(t.weatherLabel());
        dto.setQuestTitle(t.questTitle());
        dto.setTargetCategory(t.targetCategory().name());
        dto.setTargetCount(t.targetCount());
        dto.setBonusStat(t.bonusStat());
        dto.setBonusMultiplier(t.bonusMultiplier());
        dto.setCompletedCount(count);
        dto.setCompleted(count >= t.targetCount());
        
        return dto;
    }

    public String getWeatherString(int code) {
        if (code <= 3)
            return "clear";
        else if (code <= 48)
            return "cloudy";
        else if (code <= 67)
            return "rain";
        else if (code <= 77)
            return "snow";
        else
            return "storm";
    }

}

package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.stereotype.Service;

@Service
public class HabitService {
    private final WeatherService weatherService;

    public HabitService(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    public int getWeatherCode() throws Exception {
        String json = weatherService.getWeather();
        return weatherService.parseWeatherData(json);
    }

}

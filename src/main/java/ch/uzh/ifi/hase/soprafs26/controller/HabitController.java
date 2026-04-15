package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.service.HabitService;

@RestController
public class HabitController {
    private final HabitService habitService;

    HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @GetMapping("api/weather")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Map<String, Integer> MapgetWeather() throws Exception {
        int weatherCode = habitService.getWeatherCode();
        return Map.of("weatherCode", weatherCode);
    }
}

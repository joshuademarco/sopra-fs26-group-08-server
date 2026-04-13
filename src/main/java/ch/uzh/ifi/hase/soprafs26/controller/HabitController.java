package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.service.HabitService;

@RestController
public class HabitController {
    private final HabitService habitService;

    HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @GetMapping("/weather")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public int getWeather() throws Exception {
        int weatherCode = habitService.getWeatherCode();
        return weatherCode;
    }
}

package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;


public class WeatherServiceTest {
    @InjectMocks
    private WeatherService weatherService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void parseWeatherData_validJson_returnsWeatherCode() {
        String json = "{\"current\": {\"weather_code\": 45, \"temperature_2m\": 12.5}}";

        int result = weatherService.parseWeatherData(json);

        assertEquals(45, result);
    }

    @Test
    public void getMultiplier_ClearSkyphysical_returnsLowest() {
        int weatherCode = 3;
        String category = "physical";

        double result = weatherService.getMultiplier(weatherCode, category);

        assertEquals(1.0, result);
    }

    @Test
    public void getMultiplier_Cloudyphysical_returnsMid() {
        int weatherCode = 45;
        String category = "physical";

        double result = weatherService.getMultiplier(weatherCode, category);

        assertEquals(1.2, result);
    }

    @Test
    public void getMultiplier_Stormyphysical_returnsHighest() {
        int weatherCode = 95;
        String category = "physical";

        double result = weatherService.getMultiplier(weatherCode, category);

        assertEquals(2.0, result);
    }

    @Test
    public void getMultiplier_ClearSkycognitive_returnsHighest() {
        int weatherCode = 3;
        String category = "cognitive";

        double result = weatherService.getMultiplier(weatherCode, category);

        assertEquals(1.8, result);
    }

    @Test
    public void getMultiplier_Cloudycognitive_returnsMid() {
        int weatherCode = 45;
        String category = "cognitive";

        double result = weatherService.getMultiplier(weatherCode, category);

        assertEquals(1.6, result);
    }

    @Test
    public void getMultiplier_Stormycognitive_returnsLowest() {
        int weatherCode = 95;
        String category = "cognitive";

        double result = weatherService.getMultiplier(weatherCode, category);

        assertEquals(1.0, result);
    }

    @Test
    public void getMultiplier_ClearSkyemotional_returnsLow() {
        int weatherCode = 3;
        String category = "emotional";

        double result = weatherService.getMultiplier(weatherCode, category);

        assertEquals(1.0, result);
    }

    @Test
    public void getMultiplier_Cloudyemotional_returnsMid() {
        int weatherCode = 45;
        String category = "emotional";

        double result = weatherService.getMultiplier(weatherCode, category);

        assertEquals(1.2, result);
    }

    @Test
    public void getMultiplier_Stormyemotional_returnsLow() {
        int weatherCode = 95;
        String category = "emotional";

        double result = weatherService.getMultiplier(weatherCode, category);

        assertEquals(1.0, result);
    }
}

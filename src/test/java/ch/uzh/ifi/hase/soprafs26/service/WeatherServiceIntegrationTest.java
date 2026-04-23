package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class WeatherServiceIntegrationTest {
    @Autowired
    WeatherService weatherService;
    
    @Test
    void getWeather_returnsValidResponse() throws Exception {
        String result = weatherService.getWeather();
    
        assertNotNull(result);
        assertTrue(result.contains("current"));
        assertTrue(result.contains("weather_code"));
    }
}

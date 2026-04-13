package ch.uzh.ifi.hase.soprafs26.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.stereotype.Service;

import org.json.*;


@Service
public class WeatherService {
    //private String apiKey;
    private String baseUrl;

    public WeatherService() {
        //this.apiKey = "";
        this.baseUrl = "https://api.open-meteo.com/v1/forecast?latitude=47.3769&longitude=8.5417&current=temperature_2m,weather_code&timezone=Europe%2FZurich";
    }

    public String getWeather() throws Exception{
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return response.body();
    }

    public int parseWeatherData(String json) {
        JSONObject root = new JSONObject(json);
        JSONObject current = root.getJSONObject("current");
        int weatherCode = current.getInt("weather_code");

        return weatherCode;
    }

    public double getMultiplier(int weatherCode, String category) {
        if (category.equals("outdoor")) {
            if (weatherCode <= 3) return 1.0;
            else if (weatherCode <= 48) return 1.2;
            else if (weatherCode <= 67) return 1.5;
            else if (weatherCode <= 77) return 1.8;
            else return 2.0;
        }
        else if (category.equals("indoor")) {
            if (weatherCode <= 3) return 1.8;
            else if (weatherCode <= 48) return 1.6;
            else if (weatherCode <= 67) return 1.4;
            else if (weatherCode <= 77) return 1.2;
            else return 1.0;
        }
        return 1.0;
    }
}

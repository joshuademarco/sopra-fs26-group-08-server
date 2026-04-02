package ch.uzh.ifi.hase.soprafs26.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.stereotype.Service;

import org.json.*;


@Service
public class WeatherAPI {
    //private String apiKey;
    private String baseUrl;

    public WeatherAPI() {
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
}

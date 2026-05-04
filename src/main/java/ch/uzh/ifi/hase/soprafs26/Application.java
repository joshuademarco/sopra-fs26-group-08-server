package ch.uzh.ifi.hase.soprafs26;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@RestController
@SpringBootApplication
@EnableScheduling
public class Application {

	public static final String[] ALLOWED_ORIGINS = {
		"https://bettertogeter.ch",
		"https://www.bettertogeter.ch",
		"http://localhost:3000",
	};

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins(ALLOWED_ORIGINS)
						.allowCredentials(true)
						.allowedMethods("*")
						.allowedHeaders("*")
						.exposedHeaders("Set-Cookie");
			}
		};
	}


	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.setAllowedOriginPatterns(Arrays.asList("https://bettertogeter.ch", "https://www.bettertogeter.ch", "http://localhost:3000"));
		config.addAllowedMethod("GET");
		config.addAllowedMethod("POST");
		config.addAllowedMethod("PUT");
		config.addAllowedMethod("DELETE");
		config.addAllowedMethod("OPTIONS");
		config.addAllowedMethod("PATCH");
		config.addAllowedHeader("*");
		config.setExposedHeaders(Arrays.asList("Set-Cookie"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}
}

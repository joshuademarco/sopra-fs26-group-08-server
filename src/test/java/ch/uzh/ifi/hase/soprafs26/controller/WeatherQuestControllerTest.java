package ch.uzh.ifi.hase.soprafs26.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.WeatherQuestGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.service.WeatherQuestService;
import jakarta.servlet.http.Cookie;

@WebMvcTest(WeatherQuestController.class)
public class WeatherQuestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherQuestService weatherQuestService;

    @MockitoBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@test.com");
        testUser.setToken("valid-token");
        testUser.setStatus(UserStatus.ONLINE);
    }

    @Test
    public void getWeatherQuest_validToken_returns200() throws Exception {
        WeatherQuestGetDTO dto = new WeatherQuestGetDTO();
        dto.setQuestTitle("Sun's Out: Complete 3 physical habits");
        dto.setCompletedCount(0);
        dto.setCompleted(false);

        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        given(weatherQuestService.getWeatherQuest(1L)).willReturn(dto);

        mockMvc.perform(get("/users/1/weather-quest")
                .cookie(new Cookie("token", "valid-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questTitle", is("Sun's Out: Complete 3 physical habits")))
                .andExpect(jsonPath("$.completedCount", is(0)))
                .andExpect(jsonPath("$.completed", is(false)));
    }

    @Test
    public void getWeatherQuest_wrongUser_returns403() throws Exception {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setToken("other-token");
        otherUser.setStatus(UserStatus.ONLINE);

        given(userService.getUserByToken("other-token")).willReturn(otherUser);

        mockMvc.perform(get("/users/1/weather-quest")
                .cookie(new Cookie("token", "other-token")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getWeatherQuest_missingToken_returns400() throws Exception {
        mockMvc.perform(get("/users/1/weather-quest"))
                .andExpect(status().isBadRequest());
    }

}

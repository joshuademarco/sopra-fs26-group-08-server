package ch.uzh.ifi.hase.soprafs26.controller;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.AchievementKey;
import ch.uzh.ifi.hase.soprafs26.entity.Achievement;
import ch.uzh.ifi.hase.soprafs26.entity.CharacterAchievement;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.AchievementService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import jakarta.servlet.http.Cookie;

@WebMvcTest(AchievementController.class)
public class AchievementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AchievementService achievementService;

    @MockitoBean
    private UserService userService;

    private CharacterAchievement buildCharacterAchievement() {
        Achievement a = new Achievement();
        a.setId(1L);
        a.setKey(AchievementKey.FIRST_HABIT);
        a.setName("Baby Steps");
        a.setDescription("Complete your first habit");
        a.setIcon("first_habit");

        CharacterAchievement ca = new CharacterAchievement();
        ca.setId(1L);
        ca.setAchievement(a);
        ca.setEarnedAt(Instant.now());
        return ca;
    }

    @Test
    public void getAchievements_validToken_returnsAchievementList() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        CharacterAchievement ca = buildCharacterAchievement();

        given(userService.getUserByToken("token")).willReturn(user);
        given(achievementService.getAchievementsForUser(1L)).willReturn(List.of(ca));

        mockMvc.perform(get("/users/1/achievements")
                .cookie(new Cookie("token", "token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key", is("FIRST_HABIT")))
                .andExpect(jsonPath("$[0].name", is("Baby Steps")));
    }

    @Test
    public void getAchievements_emptyList_returnsEmptyArray() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        given(userService.getUserByToken("token")).willReturn(user);
        given(achievementService.getAchievementsForUser(1L)).willReturn(Collections.emptyList());

        mockMvc.perform(get("/users/1/achievements")
                .cookie(new Cookie("token", "token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void getAchievements_invalidToken_returnsUnauthorized() throws Exception {
        given(userService.getUserByToken("bad-token"))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired."));

        mockMvc.perform(get("/users/1/achievements")
                .cookie(new Cookie("token", "bad-token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}

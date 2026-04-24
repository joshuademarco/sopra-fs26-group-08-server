package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import ch.uzh.ifi.hase.soprafs26.constant.HabitFrequency;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Habit;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.HabitPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.HabitService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HabitController.class)
public class HabitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HabitService habitService;

    @MockitoBean
    private UserService userService;

    private User testUser;
    private Habit testHabit;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@test.com");
        testUser.setToken("valid-token");
        testUser.setStatus(UserStatus.ONLINE);

        testHabit = new Habit();
        testHabit.setId(1L);
        testHabit.setTitle("Morning Run");
        testHabit.setCategory(HabitCategory.PHYSICAL);
        testHabit.setFrequency(HabitFrequency.DAILY);
        testHabit.setPositive(true);
        testHabit.setWeight(1);
        testHabit.setUser(testUser);
        testHabit.setCompleted(false);
        testHabit.setStreak(0);
    }

    // helper to convert objects to JSON string
    private String asJsonString(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // GET /users/{userId}/habits
    @Test
    public void getHabits_validToken_returnsHabitList() throws Exception {
        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        given(habitService.getHabitsForUser(1L)).willReturn(List.of(testHabit));

        mockMvc.perform(get("/users/1/habits")
                .cookie(new jakarta.servlet.http.Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].title", is("Morning Run")))
            .andExpect(jsonPath("$[0].category", is("PHYSICAL")))
            .andExpect(jsonPath("$[0].completed", is(false)));
    }

    @Test
    public void getHabits_wrongUser_returnsForbidden() throws Exception {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otherUser");
        otherUser.setToken("other-token");
        otherUser.setStatus(UserStatus.ONLINE);

        // user 2 tries to access user 1 habits
        given(userService.getUserByToken("other-token")).willReturn(otherUser);

        mockMvc.perform(get("/users/1/habits")
                .cookie(new jakarta.servlet.http.Cookie("token", "other-token"))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    // POST /users/{userId}/habits
    @Test
    public void createHabit_validInput_returnsCreated() throws Exception {
        HabitPostDTO postDTO = new HabitPostDTO();
        postDTO.setTitle("Morning Run");
        postDTO.setCategory(HabitCategory.PHYSICAL);
        postDTO.setFrequency(HabitFrequency.DAILY);
        postDTO.setPositive(true);
        postDTO.setWeight(1);

        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        given(habitService.createHabit(eq(1L), any())).willReturn(testHabit);

        mockMvc.perform(post("/users/1/habits")
                .cookie(new jakarta.servlet.http.Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(postDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.title", is("Morning Run")))
            .andExpect(jsonPath("$.category", is("PHYSICAL")))
            .andExpect(jsonPath("$.frequency", is("DAILY")))
            .andExpect(jsonPath("$.positive", is(true)))
            .andExpect(jsonPath("$.completed", is(false)))
            .andExpect(jsonPath("$.streak", is(0)));
    }

    @Test
    public void createHabit_missingTitle_returnsBadRequest() throws Exception {
        // title is @NotBlank -> when missing it should fail validation
        HabitPostDTO postDTO = new HabitPostDTO();
        // deliberately not setting title
        postDTO.setCategory(HabitCategory.PHYSICAL);
        postDTO.setFrequency(HabitFrequency.DAILY);
        postDTO.setPositive(true);

        given(userService.getUserByToken("valid-token")).willReturn(testUser);

        mockMvc.perform(post("/users/1/habits")
                .cookie(new jakarta.servlet.http.Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(postDTO)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void createHabit_missingCategory_returnsBadRequest() throws Exception {
        HabitPostDTO postDTO = new HabitPostDTO();
        postDTO.setTitle("Morning Run");
        postDTO.setFrequency(HabitFrequency.DAILY);
        postDTO.setPositive(true);
        // category is @NotNull -> missing it should fail validation

        given(userService.getUserByToken("valid-token")).willReturn(testUser);

        mockMvc.perform(post("/users/1/habits")
                .cookie(new jakarta.servlet.http.Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(postDTO)))
            .andExpect(status().isBadRequest());
    }

    // PUT /users/{userId}/habits/{habitId}/complete
    @Test
    public void completeHabit_validRequest_returnsCompletedHabit() throws Exception {
        testHabit.setCompleted(true);
        testHabit.setStreak(1);

        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        given(habitService.completeHabit(1L, 1L)).willReturn(testHabit);

        mockMvc.perform(put("/users/1/habits/1/complete")
                .cookie(new jakarta.servlet.http.Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completed", is(true)))
            .andExpect(jsonPath("$.streak", is(1)));
    }

    @Test
    public void completeHabit_alreadyCompleted_returnsConflict() throws Exception {
        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        given(habitService.completeHabit(1L, 1L))
            .willThrow(new ResponseStatusException(HttpStatus.CONFLICT,
                "Habit already completed"));

        mockMvc.perform(put("/users/1/habits/1/complete")
                .cookie(new jakarta.servlet.http.Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    @Test
    public void completeHabit_habitNotFound_returnsNotFound() throws Exception {
        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        given(habitService.completeHabit(99L, 1L))
            .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Habit not found"));

        mockMvc.perform(put("/users/1/habits/99/complete")
                .cookie(new jakarta.servlet.http.Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    // DELETE /users/{userId}/habits/{habitId}
    @Test
    public void deleteHabit_validOwner_returnsNoContent() throws Exception {
        given(userService.getUserByToken("valid-token")).willReturn(testUser);

        mockMvc.perform(delete("/users/1/habits/1")
                .cookie(new jakarta.servlet.http.Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    @Test
    public void deleteHabit_habitNotFound_returnsNotFound() throws Exception {
        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Habit not found"))
            .when(habitService).deleteHabit(99L, 1L);

        mockMvc.perform(delete("/users/1/habits/99")
                .cookie(new jakarta.servlet.http.Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}

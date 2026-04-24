package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.CharacterService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CharacterController.class)
public class CharacterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CharacterService characterService;

    @MockitoBean
    private UserService userService;

    private User testUser;
    private Character testCharacter;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@test.com");
        testUser.setToken("valid-token");
        testUser.setStatus(UserStatus.ONLINE);

        testCharacter = new Character();
        testCharacter.setId(1L);
        testCharacter.setUser(testUser);
        testCharacter.setLevel(3);
        testCharacter.setExperience(75);
        testCharacter.setMaxHealth(20);
        testCharacter.setHealth(15);
        testCharacter.setStrength(5);
        testCharacter.setIntelligence(3);
        testCharacter.setResilience(2);
        testCharacter.setSkinColor("default");
        testCharacter.setType("warrior");
    }

    // --------------- GET /users/{userId}/character ---------------
    @Test
    public void getCharacter_validToken_returnsCharacter() throws Exception {
        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        given(characterService.getCharacterByUserId(1L)).willReturn(testCharacter);

        mockMvc.perform(get("/users/1/character")
                .cookie(new Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.level", is(3)))
                .andExpect(jsonPath("$.experience", is(75)))
                .andExpect(jsonPath("$.health", is(15)))
                .andExpect(jsonPath("$.maxHealth", is(20)))
                .andExpect(jsonPath("$.strength", is(5)))
                .andExpect(jsonPath("$.intelligence", is(3)))
                .andExpect(jsonPath("$.resilience", is(2)))
                .andExpect(jsonPath("$.skinColor", is("default")))
                .andExpect(jsonPath("$.type", is("warrior")));
    }

    @Test
    public void getCharacter_wrongUser_returnsForbidden() throws Exception {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setToken("other-token");
        otherUser.setStatus(UserStatus.ONLINE);

        given(userService.getUserByToken("other-token")).willReturn(otherUser);

        mockMvc.perform(get("/users/1/character")
                .cookie(new Cookie("token", "other-token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getCharacter_characterNotFound_returnsNotFound() throws Exception {
        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        given(characterService.getCharacterByUserId(1L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Character for this user was not found"));

        mockMvc.perform(get("/users/1/character")
                .cookie(new Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getCharacter_invalidToken_returnsUnauthorized() throws Exception {
        given(userService.getUserByToken("bad-token"))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Invalid Token"));

        mockMvc.perform(get("/users/1/character")
                .cookie(new Cookie("token", "bad-token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getCharacter_allStatFieldsPresent() throws Exception {
        // verify all character fields are correctly mapped through the DTO
        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        given(characterService.getCharacterByUserId(1L)).willReturn(testCharacter);

        mockMvc.perform(get("/users/1/character")
                .cookie(new Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.strength", is(testCharacter.getStrength())))
                .andExpect(jsonPath("$.intelligence", is(testCharacter.getIntelligence())))
                .andExpect(jsonPath("$.resilience", is(testCharacter.getResilience())))
                .andExpect(jsonPath("$.health", is(testCharacter.getHealth())))
                .andExpect(jsonPath("$.maxHealth", is(testCharacter.getMaxHealth())));
    }
}

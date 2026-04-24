package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Todo;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TodoPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.TodoService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import tools.jackson.databind.ObjectMapper;
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

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TodoController.class)
public class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TodoService todoService;

    @MockitoBean
    private UserService userService;

    private User testUser;
    private Todo testTodo;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@test.com");
        testUser.setToken("valid-token");
        testUser.setStatus(UserStatus.ONLINE);

        testTodo = new Todo();
        testTodo.setId(1L);
        testTodo.setTitle("Buy groceries");
        testTodo.setCategory(HabitCategory.PHYSICAL);
        testTodo.setWeight(1);
        testTodo.setUser(testUser);
        testTodo.setCompleted(false);
    }

    private String asJsonString(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // --------------- GET /users/{userId}/todos ---------------
    @Test
    public void getTodos_validToken_returnsTodoList() throws Exception {
        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        given(todoService.getTodosForUser(1L)).willReturn(List.of(testTodo));

        mockMvc.perform(get("/users/1/todos")
                .cookie(new Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Buy groceries")))
                .andExpect(jsonPath("$[0].category", is("PHYSICAL")))
                .andExpect(jsonPath("$[0].completed", is(false)));
    }

    @Test
    public void getTodos_emptyList_returnsEmptyArray() throws Exception {
        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        given(todoService.getTodosForUser(1L)).willReturn(List.of());

        mockMvc.perform(get("/users/1/todos")
                .cookie(new Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void getTodos_wrongUser_returnsForbidden() throws Exception {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setToken("other-token");
        otherUser.setStatus(UserStatus.ONLINE);

        given(userService.getUserByToken("other-token")).willReturn(otherUser);

        mockMvc.perform(get("/users/1/todos")
                .cookie(new Cookie("token", "other-token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // --------------- POST /users/{userId}/todos ---------------

    @Test
    public void createTodo_validInput_returnsCreated() throws Exception {
        TodoPostDTO postDTO = new TodoPostDTO();
        postDTO.setTitle("Buy groceries");
        postDTO.setCategory(HabitCategory.PHYSICAL);
        postDTO.setWeight(1);

        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        given(todoService.createTodo(eq(1L), any())).willReturn(testTodo);

        mockMvc.perform(post("/users/1/todos")
                .cookie(new Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(postDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Buy groceries")))
                .andExpect(jsonPath("$.category", is("PHYSICAL")))
                .andExpect(jsonPath("$.completed", is(false)));
    }

    @Test
    public void createTodo_missingTitle_returnsBadRequest() throws Exception {
        TodoPostDTO postDTO = new TodoPostDTO();
        postDTO.setCategory(HabitCategory.PHYSICAL);

        given(userService.getUserByToken("valid-token")).willReturn(testUser);

        mockMvc.perform(post("/users/1/todos")
                .cookie(new Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(postDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTodo_missingCategory_returnsBadRequest() throws Exception {
        TodoPostDTO postDTO = new TodoPostDTO();
        postDTO.setTitle("Buy groceries");

        given(userService.getUserByToken("valid-token")).willReturn(testUser);

        mockMvc.perform(post("/users/1/todos")
                .cookie(new Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(postDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTodo_withOptionalDueDate_returnsCreated() throws Exception {
        testTodo.setDueAt(Instant.now().plusSeconds(86400));

        TodoPostDTO postDTO = new TodoPostDTO();
        postDTO.setTitle("Buy groceries");
        postDTO.setCategory(HabitCategory.PHYSICAL);
        postDTO.setDueAt(Instant.now().plusSeconds(86400));

        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        given(todoService.createTodo(eq(1L), any())).willReturn(testTodo);

        mockMvc.perform(post("/users/1/todos")
                .cookie(new Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(postDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dueAt", notNullValue()));
    }

    // --------------- PUT /users/{userId}/todos/{todoId}/complete ---------------

    @Test
    public void completeTodo_validRequest_returnsCompletedTodo() throws Exception {
        testTodo.setCompleted(true);

        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        given(todoService.completeTodo(1L, 1L)).willReturn(testTodo);

        mockMvc.perform(put("/users/1/todos/1/complete")
                .cookie(new Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(true)));
    }

    @Test
    public void completeTodo_alreadyCompleted_returnsConflict() throws Exception {
        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        given(todoService.completeTodo(1L, 1L))
                .willThrow(new ResponseStatusException(HttpStatus.CONFLICT,
                        "Todo is already completed"));

        mockMvc.perform(put("/users/1/todos/1/complete")
                .cookie(new Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void completeTodo_todoNotFound_returnsNotFound() throws Exception {
        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        given(todoService.completeTodo(99L, 1L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Todo not found"));

        mockMvc.perform(put("/users/1/todos/99/complete")
                .cookie(new Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void completeTodo_wrongUser_returnsForbidden() throws Exception {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setToken("other-token");
        otherUser.setStatus(UserStatus.ONLINE);

        given(userService.getUserByToken("other-token")).willReturn(otherUser);

        mockMvc.perform(put("/users/1/todos/1/complete")
                .cookie(new Cookie("token", "other-token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // --------------- DELETE /users/{userId}/todos/{todoId} ---------------

    @Test
    public void deleteTodo_validOwner_returnsNoContent() throws Exception {
        given(userService.getUserByToken("valid-token")).willReturn(testUser);

        mockMvc.perform(delete("/users/1/todos/1")
                .cookie(new Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteTodo_todoNotFound_returnsNotFound() throws Exception {
        given(userService.getUserByToken("valid-token")).willReturn(testUser);
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found"))
                .when(todoService).deleteTodo(99L, 1L);

        mockMvc.perform(delete("/users/1/todos/99")
                .cookie(new Cookie("token", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteTodo_wrongUser_returnsForbidden() throws Exception {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setToken("other-token");
        otherUser.setStatus(UserStatus.ONLINE);

        given(userService.getUserByToken("other-token")).willReturn(otherUser);

        mockMvc.perform(delete("/users/1/todos/1")
                .cookie(new Cookie("token", "other-token"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}

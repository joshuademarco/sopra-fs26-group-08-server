package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import ch.uzh.ifi.hase.soprafs26.entity.Todo;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.TodoRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CharacterService characterService;

    @InjectMocks
    private TodoService todoService;

    private User testUser;
    private Todo testTodo;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testTodo = new Todo();
        testTodo.setId(1L);
        testTodo.setTitle("Buy groceries");
        testTodo.setCategory(HabitCategory.PHYSICAL);
        testTodo.setWeight(1);
        testTodo.setUser(testUser);
        testTodo.setCompleted(false);
    }

    // ---------------tests for user creating todo---------------
    @Test
    public void createTodo_validInput_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.save(any())).thenReturn(testTodo);

        Todo result = todoService.createTodo(1L, testTodo);

        assertNotNull(result);
        assertEquals("Buy groceries", result.getTitle());
        assertEquals(testUser, result.getUser());
        verify(todoRepository, times(1)).save(any());
    }

    @Test
    public void createTodo_userNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> todoService.createTodo(99L, testTodo));
    }

    // ---------------tests for user completing todo---------------
    @Test
    public void completeTodo_validOwner_marksComplete() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any())).thenReturn(testTodo);
        when(characterService.calculateBaseXp(1)).thenReturn(10);
        when(characterService.awardXp(any(), any(), anyInt(), anyDouble())).thenReturn(10);

        Todo result = todoService.completeTodo(1L, 1L);

        assertTrue(result.getCompleted());
        assertNotNull(result.getCompletedAt());
    }

    @Test
    public void completeTodo_awardsXpWithNoWeatherMultiplier() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any())).thenReturn(testTodo);
        when(characterService.calculateBaseXp(1)).thenReturn(10);
        when(characterService.awardXp(any(), any(), anyInt(), anyDouble())).thenReturn(10);

        todoService.completeTodo(1L, 1L);

        // todos always use 1.0 multiplier (no weather effect)
        verify(characterService, times(1)).awardXp(
                eq(1L), eq(HabitCategory.PHYSICAL), eq(10), eq(1.0));
    }

    @Test
    public void completeTodo_alreadyCompleted_throwsConflict() {
        testTodo.setCompleted(true);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        assertThrows(ResponseStatusException.class, () -> todoService.completeTodo(1L, 1L));
    }

    @Test
    public void completeTodo_wrongUser_throwsForbidden() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        assertThrows(ResponseStatusException.class, () -> todoService.completeTodo(1L, 2L));
    }

    // ---------------tests for user deleting todo---------------
    @Test
    public void deleteTodo_validOwner_deletesTodo() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        todoService.deleteTodo(1L, 1L);

        verify(todoRepository, times(1)).delete(testTodo);
    }

    @Test
    public void deleteTodo_wrongUser_throwsForbidden() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        assertThrows(ResponseStatusException.class, () -> todoService.deleteTodo(1L, 2L));
    }

    // ---------------test for user retrieving todos---------------
    @Test
    public void getTodosForUser_userNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> todoService.getTodosForUser(99L));
    }
}

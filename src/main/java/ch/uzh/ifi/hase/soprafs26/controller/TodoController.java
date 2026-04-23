package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Todo;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TodoGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TodoPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.TodoService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users/{userId}/todos")
public class TodoController {

    private final TodoService todoService;
    private final UserService userService;

    public TodoController(TodoService todoService, UserService userService) {
        this.todoService = todoService;
        this.userService = userService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TodoGetDTO> getTodos(@PathVariable Long userId,
            @CookieValue(name = "token", required = true) String token) {
        User requestingUser = userService.getUserByToken(token);
        verifyOwnership(requestingUser.getId(), userId);

        return todoService.getTodosForUser(userId).stream()
                .map(DTOMapper.INSTANCE::convertEntityToTodoGetDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TodoGetDTO createTodo(@PathVariable Long userId,
            @Valid @RequestBody TodoPostDTO todoPostDTO,
            @CookieValue(name = "token", required = true) String token) {
        User requestingUser = userService.getUserByToken(token);
        verifyOwnership(requestingUser.getId(), userId);

        Todo todo = DTOMapper.INSTANCE.convertTodoPostDTOtoEntity(todoPostDTO);
        Todo created = todoService.createTodo(userId, todo);
        return DTOMapper.INSTANCE.convertEntityToTodoGetDTO(created);
    }

    @PutMapping("/{todoId}/complete")
    @ResponseStatus(HttpStatus.OK)
    public TodoGetDTO completeTodo(@PathVariable Long userId,
            @PathVariable Long todoId,
            @CookieValue(name = "token", required = true) String token) {
        User requestingUser = userService.getUserByToken(token);
        verifyOwnership(requestingUser.getId(), userId);

        Todo todo = todoService.completeTodo(todoId, requestingUser.getId());
        return DTOMapper.INSTANCE.convertEntityToTodoGetDTO(todo);
    }

    @DeleteMapping("/{todoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTodo(@PathVariable Long userId,
            @PathVariable Long todoId,
            @CookieValue(name = "token", required = true) String token) {
        User requestingUser = userService.getUserByToken(token);
        verifyOwnership(requestingUser.getId(), userId);

        todoService.deleteTodo(todoId, requestingUser.getId());
    }

    private void verifyOwnership(Long requestingUserId, Long targetUserId) {
        if (!requestingUserId.equals(targetUserId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You can only access your own todos");
        }
    }
}

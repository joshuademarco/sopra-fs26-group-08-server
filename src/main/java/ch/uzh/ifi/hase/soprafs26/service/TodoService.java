package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Todo;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.TodoRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final CharacterService characterService;

    public TodoService(TodoRepository todoRepository,
            UserRepository userRepository,
            CharacterService characterService) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
        this.characterService = characterService;
    }

    public List<Todo> getTodosForUser(Long userId) {
        getUserOrThrow(userId);
        return todoRepository.findByUser_Id(userId);
    }

    public Todo createTodo(Long userId, Todo todo) {
        User user = getUserOrThrow(userId);
        todo.setUser(user);
        return todoRepository.save(todo);
    }

    public Todo completeTodo(Long todoId, Long userId) {
        Todo todo = getTodoOrThrow(todoId);

        if (!todo.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only complete your own todos");
        }

        if (todo.getCompleted()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Todo is already completed");
        }

        todo.complete();
        todoRepository.save(todo);

        // todo doesnt use weather multiplier
        int baseXp = characterService.calculateBaseXp(todo.getWeight());
        characterService.awardXp(userId, todo.getCategory(), baseXp, 1.0);

        return todo;
    }

    public void deleteTodo(Long todoId, Long userId) {
        Todo todo = getTodoOrThrow(todoId);
        if (!todo.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only delete your own todos");
        }
        todoRepository.delete(todo);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    private Todo getTodoOrThrow(Long todoId) {
        return todoRepository.findById(todoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Todo not found"));
    }
}

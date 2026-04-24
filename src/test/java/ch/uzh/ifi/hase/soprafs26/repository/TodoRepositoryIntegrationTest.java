package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Todo;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TodoRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TodoRepository todoRepository;

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password123");
        user.setToken("token-" + username);
        user.setStatus(UserStatus.ONLINE);
        user.setOnline(true);
        return (User) entityManager.persist(user);
    }

    private Todo createTodo(String title, User user, HabitCategory category, boolean completed) {
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setCategory(category);
        todo.setWeight(1);
        todo.setUser(user);
        todo.setCompleted(completed);
        return (Todo) entityManager.persist(todo);
    }

    @Test
    public void findByUser_IdAndCompleted_returnsOnlyIncompleteTodos() {
        User user = createUser("todoUser", "todo@test.com");

        createTodo("Done task", user, HabitCategory.PHYSICAL, true);
        createTodo("Pending task", user, HabitCategory.COGNITIVE, false);
        createTodo("Another pending", user, HabitCategory.EMOTIONAL, false);

        entityManager.flush();

        List<Todo> incomplete = todoRepository.findByUser_IdAndCompleted(user.getId(), false);
        List<Todo> complete = todoRepository.findByUser_IdAndCompleted(user.getId(), true);

        assertEquals(2, incomplete.size());
        assertEquals(1, complete.size());
        assertTrue(incomplete.stream().allMatch(t -> !t.getCompleted()));
        assertTrue(complete.stream().allMatch(Todo::getCompleted));
    }

    @Test
    public void persistTodo_allFieldsSavedCorrectly() {
        User user = createUser("fieldUser", "field@test.com");
        Instant due = Instant.now().plusSeconds(86400);

        Todo todo = new Todo();
        todo.setTitle("Submit assignment");
        todo.setDescription("SoPra M3 report");
        todo.setCategory(HabitCategory.COGNITIVE);
        todo.setWeight(3);
        todo.setUser(user);
        todo.setCompleted(false);
        todo.setDueAt(due);
        entityManager.persist(todo);
        entityManager.flush();
        entityManager.clear(); // force DB read

        Todo found = todoRepository.findById(todo.getId()).orElseThrow();

        assertEquals("Submit assignment", found.getTitle());
        assertEquals("SoPra M3 report", found.getDescription());
        assertEquals(HabitCategory.COGNITIVE, found.getCategory());
        assertEquals(3, found.getWeight());
        assertFalse(found.getCompleted());
        assertNotNull(found.getDueAt());
        assertNotNull(found.getCreatedAt()); // set by @PrePersist in Task
        assertNotNull(found.getUser());
    }

    @Test
    public void findByUser_Id_userWithNoTodos_returnsEmptyList() {
        User user = createUser("emptyUser", "empty@test.com");
        entityManager.flush();

        List<Todo> todos = todoRepository.findByUser_Id(user.getId());

        assertTrue(todos.isEmpty());
    }
}

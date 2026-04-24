package ch.uzh.ifi.hase.soprafs26;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import ch.uzh.ifi.hase.soprafs26.constant.HabitFrequency;
import ch.uzh.ifi.hase.soprafs26.entity.Group;
import ch.uzh.ifi.hase.soprafs26.entity.Habit;
import ch.uzh.ifi.hase.soprafs26.entity.Todo;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.service.GroupService;
import ch.uzh.ifi.hase.soprafs26.service.HabitService;
import ch.uzh.ifi.hase.soprafs26.service.TodoService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

@Component
public class Seeder implements ApplicationRunner {

    private final Logger log = LoggerFactory.getLogger(Seeder.class);

    private final UserRepository userRepository;
    private final UserService userService;
    private final GroupService groupService;
    private final HabitService habitService;
    private final TodoService todoService;

    private final String DEFAULT_PASSWORD = "Password123";
    private final String DEFAULT_GROUP_NAME = "Testing Guild";

    public Seeder(UserRepository userRepository,
            UserService userService,
            GroupService groupService,
            HabitService habitService,
            TodoService todoService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.groupService = groupService;
        this.habitService = habitService;
        this.todoService = todoService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded.");
            return;
        }

        log.info("Seeding database...");

        Group group = seedGroup();
        User josh = seedUser("josh", "josh@icuzh.ch", DEFAULT_PASSWORD, group);
        User ale = seedUser("ale", "ale@icuzh.ch", DEFAULT_PASSWORD, group);
        User michi = seedUser("michi", "michi@icuzh.ch", DEFAULT_PASSWORD, null);
        User leo = seedUser("leo", "leo@icuzh.ch", DEFAULT_PASSWORD, null);

        for (User user : new User[] { josh, ale, michi, leo }) {
            seedHabits(user);
            seedTodos(user);
        }

        log.info("Seeding complete.");
    }

    private Group seedGroup() {
        Group group = new Group();
        String token = userService.login("josh", DEFAULT_PASSWORD).getToken();
        group.setName(DEFAULT_GROUP_NAME);
        return groupService.createGroup(group, token);
    }

    private User seedUser(String username, String email, String password, Group group) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.addGroup(group);
        return userService.createUser(user);
    }

    private void seedHabits(User user) {
        createHabit(user, "Morning Run", "Run 5km before 8am", HabitCategory.PHYSICAL, HabitFrequency.DAILY, true, 2);
        createHabit(user, "Read 30 Minutes", "Read a non-fiction book", HabitCategory.COGNITIVE, HabitFrequency.DAILY,
                true, 1);
        createHabit(user, "Meditate", "10 minutes of mindfulness", HabitCategory.EMOTIONAL, HabitFrequency.DAILY, true,
                1);
        createHabit(user, "Skip Gym", "Avoided working out", HabitCategory.PHYSICAL, HabitFrequency.DAILY, false, 1);
    }

    private void createHabit(User user, String title, String description, HabitCategory category,
            HabitFrequency frequency, boolean positive, int weight) {
        Habit habit = new Habit();
        habit.setTitle(title);
        habit.setDescription(description);
        habit.setCategory(category);
        habit.setFrequency(frequency);
        habit.setPositive(positive);
        habit.setWeight(weight);
        habitService.createHabit(user.getId(), habit);
    }

    private void seedTodos(User user) {
        createTodo(user, "Set up workspace", "Configure IDE and install dependencies", HabitCategory.COGNITIVE,
                Instant.now().plus(3, ChronoUnit.DAYS), 1);
        createTodo(user, "Weekly review", "Reflect on last week's goals and plan next week", HabitCategory.EMOTIONAL,
                Instant.now().plus(7, ChronoUnit.DAYS), 2);
        createTodo(user, "Team meeting prep", "Prepare agenda and talking points", HabitCategory.COGNITIVE,
                Instant.now().plus(2, ChronoUnit.DAYS), 1);
    }

    private void createTodo(User user, String title, String description, HabitCategory category, Instant dueAt,
            int weight) {
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setCategory(category);
        todo.setDueAt(dueAt);
        todo.setWeight(weight);
        todoService.createTodo(user.getId(), todo);
    }
}

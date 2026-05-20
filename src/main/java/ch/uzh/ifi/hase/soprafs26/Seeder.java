package ch.uzh.ifi.hase.soprafs26;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.uzh.ifi.hase.soprafs26.constant.AchievementKey;
import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import ch.uzh.ifi.hase.soprafs26.constant.HabitFrequency;
import ch.uzh.ifi.hase.soprafs26.constant.RaidStatus;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.BossRaid;
import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.CharacterAchievement;
import ch.uzh.ifi.hase.soprafs26.entity.Group;
import ch.uzh.ifi.hase.soprafs26.entity.Habit;
import ch.uzh.ifi.hase.soprafs26.entity.HabitCompletionEvent;
import ch.uzh.ifi.hase.soprafs26.entity.RaidParticipation;
import ch.uzh.ifi.hase.soprafs26.entity.RaidTask;
import ch.uzh.ifi.hase.soprafs26.entity.Todo;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.BossRaidRepository;
import ch.uzh.ifi.hase.soprafs26.repository.CharacterAchievementRepository;
import ch.uzh.ifi.hase.soprafs26.repository.CharacterRepository;
import ch.uzh.ifi.hase.soprafs26.repository.HabitCompletionEventRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RaidParticipationRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RaidTaskRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.service.AchievementService;
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
    private final BossRaidRepository bossRaidRepository;
    private final RaidTaskRepository raidTaskRepository;
    private final RaidParticipationRepository raidParticipationRepository;
    private final HabitCompletionEventRepository completionEventRepository;
    private final CharacterRepository characterRepository;
    private final CharacterAchievementRepository characterAchievementRepository;
    private final AchievementService achievementService;

    public Seeder(UserRepository userRepository,
            UserService userService,
            GroupService groupService,
            HabitService habitService,
            TodoService todoService,
            BossRaidRepository bossRaidRepository,
            RaidTaskRepository raidTaskRepository,
            RaidParticipationRepository raidParticipationRepository,
            HabitCompletionEventRepository completionEventRepository,
            CharacterRepository characterRepository,
            CharacterAchievementRepository characterAchievementRepository,
            AchievementService achievementService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.groupService = groupService;
        this.habitService = habitService;
        this.todoService = todoService;
        this.bossRaidRepository = bossRaidRepository;
        this.raidTaskRepository = raidTaskRepository;
        this.raidParticipationRepository = raidParticipationRepository;
        this.completionEventRepository = completionEventRepository;
        this.characterRepository = characterRepository;
        this.characterAchievementRepository = characterAchievementRepository;
        this.achievementService = achievementService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded.");
            return;
        }

        log.info("Seeding database...");

        User josh = seedUser("josh", "josh@icuzh.ch", "Password123", "josh");
        User ale = seedUser("ale", "ale@icuzh.ch", "Password123", "ale");
        User michi = seedUser("michi", "michi@icuzh.ch", "Password123", "michi");
        User leo = seedUser("leo", "leo@icuzh.ch", "Password123", "leo");

        for (User user : new User[] { josh, ale, michi, leo }) {
            List<Habit> habits = seedHabits(user);
            seedTodos(user);
            seedCompletionEvents(user, habits);
        }

        Group group = seedGroup();

        for (User user : new User[] { ale, michi, leo }) {
            groupService.joinGroup("Testing Guild", "Password123", user.getToken());
        }
        seedRaid(group, new User[] { josh, ale, michi, leo });

        log.info("Seeding complete.");
    }

    private Group seedGroup() {
        Group group = new Group();
        group.setName("Testing Guild");
        group.setPassword("Password123");
        User owner = userRepository.findByUsername("josh");
        return groupService.createGroup(group, owner.getToken());
    }

    private User seedUser(String username, String email, String password) {
        return seedUser(username, email, password, null, null);
    }

    private User seedUser(String username, String email, String password, String characterType) {
        return seedUser(username, email, password, null, characterType);
    }

    private User seedUser(String username, String email, String password, Group group, String characterType) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setStatus(UserStatus.OFFLINE);

        if (group != null) {
            user.addGroup(group);
        }

        return userService.createUser(user, characterType);
    }

    private List<Habit> seedHabits(User user) {
        List<Habit> habits = new ArrayList<>();
        habits.add(createHabit(user, "Morning Run", "Run 5km before 8am", HabitCategory.PHYSICAL, HabitFrequency.DAILY,
                true, 3));
        habits.add(createHabit(user, "Read 30 Minutes", "Read a non-fiction book", HabitCategory.COGNITIVE,
                HabitFrequency.DAILY, true, 2));
        habits.add(createHabit(user, "Meditate", "10 minutes of mindfulness", HabitCategory.EMOTIONAL,
                HabitFrequency.DAILY, true, 1));
        habits.add(createHabit(user, "Skip Gym", "Avoided working out", HabitCategory.PHYSICAL, HabitFrequency.DAILY,
                false, 2));
        return habits;
    }

    private Habit createHabit(User user, String title, String description, HabitCategory category,
            HabitFrequency frequency, boolean positive, int weight) {
        Habit habit = new Habit();
        habit.setTitle(title);
        habit.setDescription(description);
        habit.setCategory(category);
        habit.setFrequency(frequency);
        habit.setPositive(positive);
        habit.setWeight(weight);
        return habitService.createHabit(user.getId(), habit);
    }

    private void seedCompletionEvents(User user, List<Habit> habits) {
        List<Habit> positive = habits.stream().filter(Habit::getPositive).collect(Collectors.toList());
        Random rng = new Random(user.getId()); // fixed seed per user
        Instant now = Instant.now();

        List<HabitCompletionEvent> events = new ArrayList<>();
        for (int daysAgo = 364; daysAgo >= 0; daysAgo--) {
            if (rng.nextDouble() > 0.80)
                continue;

            int count = 1 + rng.nextInt(positive.size()); // 1 to 3 completions
            Instant dayBase = now.minus(daysAgo, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);

            for (int i = 0; i < count; i++) {
                Habit habit = positive.get(rng.nextInt(positive.size()));
                HabitCompletionEvent event = new HabitCompletionEvent();
                event.setHabit(habit);
                event.setUser(user);
                event.setBaseXp(habit.getWeight() * 10);
                event.setMultipliedXp(habit.getWeight() * 10);
                event.setWeatherMultiplier(1.0);
                event.setWeatherCode(0);
                event.setCompletedAt(dayBase.plus(rng.nextInt(86400), ChronoUnit.SECONDS));
                events.add(event);
            }
        }
        completionEventRepository.saveAll(events);

        Character character = characterRepository.findByUserId(user.getId());
        for (HabitCompletionEvent event : events) {
            character.addExperience(event.getMultipliedXp());
            character.increaseStat(event.getHabit().getCategory());
        }
        characterRepository.save(character);

        achievementService.checkHabitAchievements(user.getId(), 7);
        achievementService.checkStatAchievements(user.getId(), AchievementKey.STRENGTH_25, character.getStrength());
        achievementService.checkStatAchievements(user.getId(), AchievementKey.INTELLIGENCE_25,
                character.getIntelligence());
        achievementService.checkStatAchievements(user.getId(), AchievementKey.RESILIENCE_25, character.getResilience());

        Instant firstDay = now.minus(364, ChronoUnit.DAYS);
        for (CharacterAchievement ca : characterAchievementRepository.findByCharacter(character)) {
            Instant earnedAt = switch (ca.getAchievement().getKey()) {
                case FIRST_HABIT -> firstDay;
                case STREAK_3 -> firstDay.plus(3, ChronoUnit.DAYS);
                case STREAK_7 -> firstDay.plus(7, ChronoUnit.DAYS);
                default -> firstDay.plus(60, ChronoUnit.DAYS);
            };
            ca.setEarnedAt(earnedAt);
        }
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

    private void seedRaid(Group group, User[] members) {
        BossRaid raid = new BossRaid();
        raid.setGroup(group);
        raid.setName("Innere Schweinehund");
        raid.setHealth(1000);
        raid.setMaxHealth(1000);
        raid.setDurationSeconds(300);
        raid.setStatus(RaidStatus.SCHEDULED);
        raid.setScheduledTime(Instant.now().plus(1, ChronoUnit.MINUTES));
        raid = bossRaidRepository.save(raid);

        // taskOrder 1 — 60s window each
        createRaidTask(raid, members[0], "Make your bed", "Tidy your bed right now", HabitCategory.PHYSICAL, 80, 10, 1,
                60);
        createRaidTask(raid, members[1], "Stretch your legs", "Strech both of your legs now!", HabitCategory.PHYSICAL,
                80, 10, 1, 60);
        createRaidTask(raid, members[2], "Deep breathing", "Take 10 slow deep breaths", HabitCategory.EMOTIONAL, 80, 10,
                1, 60);
        createRaidTask(raid, members[3], "Drink a glass of water", "Finish one full glass", HabitCategory.PHYSICAL, 80,
                10, 1, 60);

        // taskOrder 2 — 60s window each
        createRaidTask(raid, members[0], "Desk cleanup", "Remove clutter from your desk", HabitCategory.COGNITIVE, 90,
                12, 2, 60);
        createRaidTask(raid, members[1], "Do 10 Push-ups", "Perform 10 push-ups", HabitCategory.PHYSICAL, 90, 12, 2,
                60);
        createRaidTask(raid, members[2], "Posture check",
                "Make sure you sit and standup straight. Remain like this until yountil otherwise",
                HabitCategory.PHYSICAL, 90, 12, 2, 15);
        createRaidTask(raid, members[3], "Positivity Check", "Write down 3 things positive about yourself",
                HabitCategory.EMOTIONAL, 90, 12, 2, 60);

        // taskOrder 3 — 60s window each
        createRaidTask(raid, members[0], "Gratefulness Check", "Write down 3 things you're grateful for",
                HabitCategory.COGNITIVE, 100, 15, 3, 45);
        createRaidTask(raid, members[1], "Answer unread messages", "Reply to 3 unread messages",
                HabitCategory.COGNITIVE, 100, 15, 3, 60);
        createRaidTask(raid, members[2], "Refill water bottle", "Refill and place it on your desk",
                HabitCategory.PHYSICAL, 100, 15, 3, 30);
        createRaidTask(raid, members[3], "One positive message", "Send an encouraging message to someone",
                HabitCategory.EMOTIONAL, 100, 15, 3, 60);

        // taskOrder 4 — 60s window each
        createRaidTask(raid, members[0], "Plan top 3 tasks", "List your top 3 priorities for today",
                HabitCategory.COGNITIVE, 110, 18, 4, 60);
        createRaidTask(raid, members[1], "Eye break", "Look away from the screen for 60 seconds",
                HabitCategory.EMOTIONAL, 110, 18, 4, 60);
        createRaidTask(raid, members[2], "10 squats", "Do 10 bodyweight squats", HabitCategory.PHYSICAL, 110, 18, 4,
                60);
        createRaidTask(raid, members[3], "Clear one small task", "Complete one pending micro-task",
                HabitCategory.COGNITIVE, 110, 18, 4, 60);

        for (User user : members) {
            RaidParticipation participation = new RaidParticipation();
            participation.setUser(user);
            participation.setBossRaid(raid);
            raidParticipationRepository.save(participation);
        }
    }

    private void createRaidTask(BossRaid raid, User assignedUser, String title, String description,
            HabitCategory category, int successfulDamage, int groupDamage, int taskOrder, int timeLimitSeconds) {
        RaidTask task = new RaidTask();
        task.setRaid(raid);
        task.setAssignedUser(assignedUser);
        task.setTitle(title);
        task.setDescription(description);
        task.setCategory(category);
        task.setSuccessfulDamage(successfulDamage);
        task.setGroupDamage(groupDamage);
        task.setTaskOrder(taskOrder);
        task.setTimeLimitSeconds(timeLimitSeconds);
        raidTaskRepository.save(task);
    }
}

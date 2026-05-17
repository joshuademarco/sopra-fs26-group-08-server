package ch.uzh.ifi.hase.soprafs26.service;

import java.time.Instant;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import ch.uzh.ifi.hase.soprafs26.constant.RaidStatus;
import ch.uzh.ifi.hase.soprafs26.entity.BossRaid;
import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.Group;
import ch.uzh.ifi.hase.soprafs26.entity.Item;
import ch.uzh.ifi.hase.soprafs26.entity.RaidParticipation;
import ch.uzh.ifi.hase.soprafs26.entity.RaidTask;
import ch.uzh.ifi.hase.soprafs26.entity.RaidTaskCompletion;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.BossRaidRepository;
import ch.uzh.ifi.hase.soprafs26.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RaidParticipationRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RaidTaskCompletionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RaidTaskRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.FreeSlotGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RaidGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RaidMemberDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RaidPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RaidTaskDTO;

@Service
public class RaidService {

    private final BossRaidRepository bossRaidRepository;
    private final RaidParticipationRepository raidParticipationRepository;
    private final UserRepository userRepository;
    private final RaidTaskRepository raidTaskRepository;
    private final RaidTaskCompletionRepository raidTaskCompletionRepository;
    private final CalendarService calendarService;
    private final GroupRepository groupRepository;
    private final RaidLiveService raidLiveService;
    private final ch.uzh.ifi.hase.soprafs26.service.CharacterLiveService characterLiveService;
    private final ItemService itemService;

    @Autowired
    public RaidService(BossRaidRepository bossRaidRepository,
            RaidParticipationRepository raidParticipationRepository,
            UserRepository userRepository, RaidTaskRepository raidTaskRepository,
            RaidTaskCompletionRepository raidTaskCompletionRepository,
            CalendarService calendarService,
            GroupRepository groupRepository,
            RaidLiveService raidLiveService,
            ch.uzh.ifi.hase.soprafs26.service.CharacterLiveService characterLiveService,
            ItemService itemService) {
        this.bossRaidRepository = bossRaidRepository;
        this.raidParticipationRepository = raidParticipationRepository;
        this.userRepository = userRepository;
        this.raidTaskRepository = raidTaskRepository;
        this.raidTaskCompletionRepository = raidTaskCompletionRepository;
        this.calendarService = calendarService;
        this.groupRepository = groupRepository;
        this.raidLiveService = raidLiveService;
        this.characterLiveService = characterLiveService;
        this.itemService = itemService;
    }

    public BossRaid createRaid(Long groupId, RaidPostDTO dto) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        BossRaid raid = new BossRaid();
        raid.setGroup(group);
        raid.setName(dto.getName());
        raid.setDurationSeconds(dto.getDurationSeconds());
        raid.setHealth(dto.getHealth());
        raid.setMaxHealth(dto.getHealth());
        raid.setStatus(RaidStatus.SCHEDULED);

        raid = bossRaidRepository.save(raid);

        int windowDays = dto.getSearchWindowDays() != null ? dto.getSearchWindowDays() : 7;
        if (tryAutoSchedule(raid, group, windowDays)) {
            createCalendarEventsForRaid(raid);
        }

        return raid;
    }

    public BossRaid rescheduleRaid(Long raidId, int windowDays) {
        BossRaid raid = bossRaidRepository.findById(raidId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Raid not found"));

        if (raid.getStatus() != RaidStatus.SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only SCHEDULED raids can be rescheduled");
        }

        if (tryAutoSchedule(raid, raid.getGroup(), windowDays)) {
            createCalendarEventsForRaid(raid);
        }
        return raid;
    }

    public List<BossRaid> getRaidsByGroup(Long groupId) {
        return bossRaidRepository.findByGroupId(groupId);
    }

    public BossRaid getRaid(Long raidId) {
        return bossRaidRepository.findById(raidId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Raid not found"));
    }

    public RaidGetDTO convertEntityToRaidGetDTO(BossRaid raid) {
        RaidGetDTO dto = new RaidGetDTO();
        dto.setId(raid.getId());
        dto.setName(raid.getName());
        dto.setStatus(raid.getStatus());
        dto.setScheduledTime(raid.getScheduledTime());
        dto.setHealth(raid.getHealth());
        dto.setMaxHealth(raid.getMaxHealth());
        dto.setDurationSeconds(raid.getDurationSeconds());
        dto.setStartedAt(raid.getStartedAt());

        Group group = raid.getGroup();
        if (group != null) {
            dto.setGroupId(group.getId());
            dto.setGroupName(group.getName());

            List<RaidParticipation> participations = raidParticipationRepository.findByBossRaidId(raid.getId());
            Map<Long, RaidParticipation> participationByUserId = participations.stream()
                    .collect(Collectors.toMap(p -> p.getUser().getId(), p -> p));

            List<User> raidMembers = new ArrayList<>(group.getUsers());
            List<RaidMemberDTO> members = new ArrayList<>();
            for (User user : raidMembers) {
                RaidMemberDTO member = new RaidMemberDTO();
                member.setUserId(user.getId());
                member.setUsername(user.getUsername());
                member.setOnline(user.getStatus());

                RaidParticipation p = participationByUserId.get(user.getId());
                member.setJoined(p != null && p.getAccepted());
                member.setAccepted(p != null ? p.getAccepted() : null);
                member.setTasksCompleted(p != null ? p.getTasksCompleted() : 0);
                member.setTasksFailed(p != null ? p.getTasksFailed() : 0);
                member.setDamageDealt(p != null ? p.getDamageDealt() : 0);
                member.setXpEarned(p != null ? p.getXpEarned() : 0);
                member.setMvp(p != null && Boolean.TRUE.equals(p.getMvp()));

                Character character = user.getCharacter();
                member.setHealth(character.getHealth());
                member.setMaxHealth(character.getMaxHealth());
                member.setCharacterType(character.getType());
                members.add(member);
            }
            dto.setUsers(members);
        }

        List<RaidTask> tasks = raidTaskRepository.findByRaid(raid);
        tasks.sort(Comparator.comparingInt(task -> (task.getTaskOrder() != null ? task.getTaskOrder() : 0)));

        // track cumulative window offset per user
        Map<Long, Integer> userWindowOffset = new HashMap<>();
        List<RaidTaskDTO> taskDTOs = new ArrayList<>();
        for (RaidTask task : tasks) {
            taskDTOs.add(buildRaidTaskDTO(task, userWindowOffset));
        }
        dto.setTasks(taskDTOs);

        return dto;
    }

    private void endRaidWithRewards(BossRaid raid, RaidStatus outcome) {
        raid.endRaid(outcome);
        List<RaidParticipation> participations = raidParticipationRepository.findByBossRaidId(raid.getId());
        if (participations.isEmpty())
            return;

        RaidParticipation top = null;
        List<Item> items = outcome == RaidStatus.DEFEATED ? itemService.getAllItems() : List.of();
        for (RaidParticipation p : participations) {
            int xp;
            if (outcome == RaidStatus.DEFEATED) {
                xp = (p.getDamageDealt() != null ? p.getDamageDealt() : 0)
                        + (p.getTasksCompleted() != null ? p.getTasksCompleted() : 0) * 10;
                if (!items.isEmpty() && Math.random() < 0.25) {
                    Long userId = p.getUser().getId();
                    int randomItem = (int)(Math.random() * items.size());
                    Long itemId = items.get(randomItem).getId();
                    try {
                        itemService.grantItem(userId, itemId);
                    } catch (ResponseStatusException ignored) {
                    }  
                }
            } else {
                xp = (p.getTasksCompleted() != null ? p.getTasksCompleted() : 0) * 5;
            }
            p.setXpEarned(xp);
            p.setMvp(false);
            if (top == null
                    || (p.getDamageDealt() != null ? p.getDamageDealt()
                            : 0) > (top.getDamageDealt() != null ? top.getDamageDealt() : 0)) {
                top = p;
            }
        }

        if (outcome == RaidStatus.DEFEATED && top != null
                && (top.getDamageDealt() != null ? top.getDamageDealt() : 0) > 0) {
            top.setMvp(true);
            top.setXpEarned(top.getXpEarned() + 50);
        }

        for (RaidParticipation p : participations) {
            int xp = p.getXpEarned();
            if (xp > 0) {
                Character character = p.getUser().getCharacter();
                if (character != null) {
                    character.addExperience(xp);
                    try {
                        characterLiveService.broadcastCharacterUpdate(p.getUser().getId(), character);
                    } catch (Exception ignored) {
                    }
                }
            }
            raidParticipationRepository.save(p);
        }
        userRepository.saveAll(participations.stream().map(RaidParticipation::getUser).collect(Collectors.toList()));
    }

    private RaidTaskDTO buildRaidTaskDTO(RaidTask task, Map<Long, Integer> userWindowOffset) {
        RaidTaskDTO taskDTO = new RaidTaskDTO();
        taskDTO.setId(task.getId());
        taskDTO.setTitle(task.getTitle());
        taskDTO.setDescription(task.getDescription());
        taskDTO.setSuccessfulDamage(task.getSuccessfulDamage());
        taskDTO.setGroupDamage(task.getGroupDamage());
        taskDTO.setTimeLimitSeconds(task.getTimeLimitSeconds());
        taskDTO.setTaskOrder(task.getTaskOrder());

        Long assignedUserId = task.getAssignedUser() != null ? task.getAssignedUser().getId() : null;
        taskDTO.setAssignedUserId(assignedUserId);

        int windowStartSeconds = userWindowOffset.getOrDefault(assignedUserId, 0);
        taskDTO.setWindowStartSeconds(windowStartSeconds);
        if (assignedUserId != null && task.getTimeLimitSeconds() != null) {
            userWindowOffset.put(assignedUserId, windowStartSeconds + task.getTimeLimitSeconds());
        }

        List<RaidTaskCompletion> completions = raidTaskCompletionRepository.findByRaidTask(task);

        List<Long> completedByUserIds = completions.stream()
                .map(completion -> completion.getParticipation().getUser().getId())
                .collect(Collectors.toList());

        List<Long> successfullyCompletedByUsers = completions.stream()
                .filter(completion -> Boolean.TRUE.equals(completion.getSuccess()))
                .map(completion -> completion.getParticipation().getUser().getId())
                .collect(Collectors.toList());

        taskDTO.setCompletedByUserIds(completedByUserIds);
        taskDTO.setSuccessfullyCompletedByUsers(successfullyCompletedByUsers);

        return taskDTO;
    }

    public void activateDueRaids() {
        List<BossRaid> due = bossRaidRepository.findByStatusAndScheduledTimeBefore(RaidStatus.SCHEDULED, Instant.now());
        for (BossRaid raid : due) {
            long confirmed = raidParticipationRepository.countByBossRaidIdAndAccepted(raid.getId(), true);
            if (confirmed < 2) {
                deleteRaid(raid);
                broadcastRaidDeletion(raid);
                continue;
            }
            raid.startRaid();
            bossRaidRepository.save(raid);
            broadcastRaidUpdate(raid);
        }
    }

    private boolean isCharacterKnockedOut(User user) {
        Character character = user.getCharacter();
        return character != null && character.getHealth() != null && character.getHealth() <= 0;
    }

    public void expireActiveRaids() {
        List<BossRaid> active = bossRaidRepository.findByStatus(RaidStatus.ACTIVE);
        Instant now = Instant.now();
        for (BossRaid raid : active) {
            if (raid.getStartedAt() == null) {
                continue;
            }
            Instant deadline = raid.getStartedAt().plusSeconds(raid.getDurationSeconds());
            if (now.isAfter(deadline)) {
                endRaidWithRewards(raid, RaidStatus.FAILED);
                bossRaidRepository.save(raid);
                broadcastRaidUpdate(raid);
            }
        }
    }

    public RaidParticipation joinRaid(Long raidId, String token) {
        BossRaid raid = bossRaidRepository.findById(raidId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Raid not found"));

        User user = resolveUser(token);

        if (isCharacterKnockedOut(user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Your character is knocked out and cannot join the raid!");
        }

        java.util.Optional<RaidParticipation> existing = raidParticipationRepository.findByBossRaidAndUser(raid, user);
        if (existing.isPresent()) {
            RaidParticipation p = existing.get();
            if (Boolean.TRUE.equals(p.getAccepted())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Already joined this raid");
            }
            // Previously declined or no response — allow joining an active raid
            p.setAccepted(true);
            return raidParticipationRepository.save(p);
        }

        RaidParticipation participation = new RaidParticipation();
        participation.setUser(user);
        participation.setBossRaid(raid);
        participation.setAccepted(true);
        return raidParticipationRepository.save(participation);
    }

    public void rsvpRaid(Long raidId, boolean accepted, String token) {
        BossRaid raid = bossRaidRepository.findById(raidId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Raid not found"));

        if (raid.getStatus() != RaidStatus.SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "RSVP is only available for scheduled raids");
        }

        User user = resolveUser(token);

        RaidParticipation participation = raidParticipationRepository.findByBossRaidAndUser(raid, user)
                .orElseGet(() -> {
                    RaidParticipation p = new RaidParticipation();
                    p.setUser(user);
                    p.setBossRaid(raid);
                    return p;
                });

        participation.setAccepted(accepted);
        raidParticipationRepository.save(participation);
    }

    // Keep session open due to LAZY relations
    @Transactional
    public RaidTaskCompletion completeTask(Long raidId, Long taskId, Boolean success, String token) {
        BossRaid raid = bossRaidRepository.findById(raidId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Raid not found"));

        if (raid.getStatus() != RaidStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Raid is not active");
        }

        User user = resolveUser(token);

        RaidTask task = raidTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        RaidParticipation participation = raidParticipationRepository.findByBossRaidAndUser(raid, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not participating in this raid"));

        if (isCharacterKnockedOut(user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Your character is knocked out for this raid");
        }

        raidTaskCompletionRepository.findByRaidTaskAndParticipation(task, participation).ifPresent(c -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Task already completed");
        });

        RaidTaskCompletion completion = new RaidTaskCompletion();
        completion.setParticipation(participation);
        completion.setRaidTask(task);
        completion.setSuccess(success);
        raidTaskCompletionRepository.save(completion);

        if (success) {
            int damage = task.getSuccessfulDamage() != null ? task.getSuccessfulDamage() : 0;
            List<RaidTask> tasks = raidTaskRepository.findByRaid(raid);
            tasks.sort(Comparator.comparingInt(t -> (t.getTaskOrder() != null ? t.getTaskOrder() : 0)));
            int windowStartSeconds = tasks.stream()
                    .filter(t -> t.getAssignedUser().equals(task.getAssignedUser())
                            && t.getTaskOrder() < task.getTaskOrder())
                    .mapToInt(t -> t.getTimeLimitSeconds() != null ? t.getTimeLimitSeconds() : 0)
                    .sum();
            Instant taskWindowStart = raid.getStartedAt().plusSeconds(windowStartSeconds);
            Long elapsed = Duration.between(taskWindowStart, completion.getCompletedAt()).getSeconds();
            double timeLeftRatio = 1.0 - ((double) elapsed / task.getTimeLimitSeconds());
            if (timeLeftRatio >= 0.75) {
                damage = (int) (damage * 1.1);
            } else if (timeLeftRatio >= 0.5) {
                damage = (int) (damage * 1.05);
            }

            raid.applyDamage(damage);
            participation.setTasksCompleted(participation.getTasksCompleted() + 1);
            participation.setDamageDealt(participation.getDamageDealt() + damage);
        } else {
            participation.setTasksFailed(participation.getTasksFailed() + 1);
            int groupDmg = task.getGroupDamage() != null ? task.getGroupDamage() : 0;
            if (groupDmg > 0) {
                applyGroupDamageToMembers(raidId, groupDmg);
            }
        }
        raidParticipationRepository.save(participation);

        // applyDamage may have already set DEFEATED above; reward in that case too
        if (raid.getStatus() == RaidStatus.DEFEATED || raid.getStatus() == RaidStatus.FAILED) {
            endRaidWithRewards(raid, raid.getStatus());
        } else if (allParticipantsKnockedOut(raid)) {
            endRaidWithRewards(raid, RaidStatus.FAILED);
        } else if (allTasksCompleted(raid)) {
            endRaidWithRewards(raid, RaidStatus.DEFEATED);
        }
        bossRaidRepository.save(raid);

        broadcastRaidUpdate(raid);

        return completion;
    }

    // Keep session open due to LAZY relations; causes LazyInitializationException
    // otherwise
    @Transactional
    public void expireOverdueTasks() {
        List<BossRaid> activeRaids = bossRaidRepository.findByStatus(RaidStatus.ACTIVE);
        for (BossRaid raid : activeRaids) {
            if (raid.getStartedAt() == null) {
                continue;
            }
            Instant now = Instant.now();

            List<RaidTask> tasks = raidTaskRepository.findByRaid(raid);
            tasks.sort(Comparator.comparingInt(task -> (task.getTaskOrder() != null ? task.getTaskOrder() : 0)));

            Map<Long, Integer> userWindowOffset = new HashMap<>();
            for (RaidTask task : tasks) {
                if (task.getAssignedUser() == null || task.getTimeLimitSeconds() == null)
                    continue;
                Long userId = task.getAssignedUser().getId();

                int windowStart = userWindowOffset.getOrDefault(userId, 0);
                int windowEnd = windowStart + task.getTimeLimitSeconds();
                userWindowOffset.put(userId, windowEnd);

                Instant windowEndTime = raid.getStartedAt().plusSeconds(windowEnd);
                if (!now.isAfter(windowEndTime))
                    continue;

                List<RaidTaskCompletion> completions = raidTaskCompletionRepository.findByRaidTask(task);
                if (!completions.isEmpty())
                    continue;

                User user = userRepository.findById(userId).orElse(null);
                if (user == null)
                    continue;

                RaidParticipation participation = raidParticipationRepository
                        .findByBossRaidAndUser(raid, user).orElse(null);
                if (participation == null)
                    continue;

                RaidTaskCompletion completion = new RaidTaskCompletion();
                completion.setParticipation(participation);
                completion.setRaidTask(task);
                completion.setSuccess(false);
                raidTaskCompletionRepository.save(completion);

                participation.setTasksFailed(participation.getTasksFailed() + 1);
                raidParticipationRepository.save(participation);

                int groupDmg = task.getGroupDamage();
                if (groupDmg > 0) {
                    applyGroupDamageToMembers(raid.getId(), groupDmg);
                }

                if (raid.getStatus() == RaidStatus.ACTIVE && allParticipantsKnockedOut(raid)) {
                    endRaidWithRewards(raid, RaidStatus.FAILED);
                    bossRaidRepository.save(raid);
                } else if (raid.getStatus() == RaidStatus.ACTIVE && allTasksCompleted(raid)) {
                    endRaidWithRewards(raid, RaidStatus.DEFEATED);
                    bossRaidRepository.save(raid);
                }

                broadcastRaidUpdate(raid);
            }
        }
    }

    private void applyGroupDamageToMembers(Long raidId, int groupDamage) {
        List<RaidParticipation> participations = raidParticipationRepository.findByBossRaidId(raidId);
        for (RaidParticipation participation : participations) {
            if (isCharacterKnockedOut(participation.getUser())) {
                continue;
            }
            User member = participation.getUser();
            Character character = member.getCharacter();

            character.hit(groupDamage);
            userRepository.save(member);
            try {
                characterLiveService.broadcastCharacterUpdate(member.getId(), character);
            } catch (Exception ignored) {
            }
        }
    }

    private boolean allParticipantsKnockedOut(BossRaid raid) {
        List<RaidParticipation> participations = raidParticipationRepository.findByBossRaidId(raid.getId());
        if (participations.isEmpty()) {
            return false;
        }
        for (RaidParticipation p : participations) {
            if (!isCharacterKnockedOut(p.getUser())) {
                return false;
            }
        }
        return true;
    }

    private void broadcastRaidUpdate(BossRaid raid) {
        raidLiveService.broadcastRaidUpdate(
                raid.getId(),
                raid.getGroup().getId(),
                raid.getHealth(),
                raid.getMaxHealth(),
                raid.getStatus().name(),
                memberHealthSnapshot(raid));
    }

    private List<RaidMemberDTO> memberHealthSnapshot(BossRaid raid) {
        List<RaidMemberDTO> snapshot = new ArrayList<>();
        List<User> raidMembers = raid.getStatus() == RaidStatus.ACTIVE
                ? getAliveGroupUsers(raid.getGroup())
                : new ArrayList<>(raid.getGroup().getUsers());
        for (User user : raidMembers) {
            RaidMemberDTO m = new RaidMemberDTO();
            m.setUserId(user.getId());
            Character character = user.getCharacter();
            m.setHealth(character.getHealth());
            m.setMaxHealth(character.getMaxHealth());
            snapshot.add(m);
        }
        return snapshot;
    }

    private List<User> getAliveGroupUsers(Group group) {
        return group.getUsers().stream()
                .filter(user -> !isCharacterKnockedOut(user))
                .collect(Collectors.toList());
    }

    private User resolveUser(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token");
        }
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        return user;
    }

    private int calculateBossHealth(BossRaid raid, int numUsers) {
        int totalDamage = raidTaskRepository.findByRaid(raid).stream()
                .mapToInt(task -> (task.getSuccessfulDamage() != null ? task.getSuccessfulDamage() : 0) +
                        (task.getGroupDamage() != null ? task.getGroupDamage() : 0))
                .sum();

        int baseHealth = Math.max(300, (int) Math.ceil(totalDamage * 0.80));
        double sizeScaling = Math.max(0.9, Math.min(1.1, 1.0 + (numUsers - 3) * 0.05));

        return (int) Math.ceil(baseHealth * sizeScaling);
    }

    @Transactional
    public BossRaid quickStartRaid(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        BossRaid raid = new BossRaid();
        raid.setGroup(group);
        raid.setName("Innere Schweinehund");
        raid.setDurationSeconds(300);
        // 20-second join window — activateDueRaids fires the actual ACTIVE transition
        raid.setScheduledTime(Instant.now().plusSeconds(20));
        raid.setStatus(RaidStatus.SCHEDULED);
        raid.setHealth(1);
        raid.setMaxHealth(1);
        raid = bossRaidRepository.save(raid);

        List<User> members = getAliveGroupUsers(group);
        if (members.isEmpty()) {
            int bossHealth = calculateBossHealth(raid, 0);
            raid.setHealth(bossHealth);
            raid.setMaxHealth(bossHealth);
            bossRaidRepository.save(raid);
            broadcastRaidUpdate(raid);
            return raid;
        }

        createQuickTasksForRaid(raid, members);

        int bossHealth = calculateBossHealth(raid, members.size());
        raid.setHealth(bossHealth);
        raid.setMaxHealth(bossHealth);

        // Pre-accept all alive members; they may decline within the 20-second window
        for (User member : members) {
            RaidParticipation participation = new RaidParticipation();
            participation.setUser(member);
            participation.setBossRaid(raid);
            participation.setAccepted(true);
            raidParticipationRepository.save(participation);
        }
        // Also invite the rest of the group as pending
        inviteAllGroupMembers(raid, group);

        bossRaidRepository.save(raid);
        broadcastRaidUpdate(raid);

        return raid;
    }

    private void createQuickTask(BossRaid raid, User assignedUser, String title, String description,
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

    private void createQuickTasksForRaid(BossRaid raid, List<User> members) {
        if (members.isEmpty())
            return;
        int size = members.size();
        createQuickTask(raid, members.get(0 % size), "Make your bed", "Tidy your bed right now", HabitCategory.PHYSICAL,
                80, 1, 1, 60);
        createQuickTask(raid, members.get(1 % size), "Stretch your legs", "Stretch both of your legs now!",
                HabitCategory.PHYSICAL, 80, 1, 1, 60);
        createQuickTask(raid, members.get(2 % size), "Deep breathing", "Take 10 slow deep breaths",
                HabitCategory.EMOTIONAL, 80, 1, 1, 60);
        createQuickTask(raid, members.get(3 % size), "Drink a glass of water", "Finish one full glass",
                HabitCategory.PHYSICAL, 80, 1, 1, 60);
        createQuickTask(raid, members.get(0 % size), "Desk cleanup", "Remove clutter from your desk",
                HabitCategory.COGNITIVE, 90, 2, 2, 60);
        createQuickTask(raid, members.get(1 % size), "Do 10 Push-ups", "Perform 10 push-ups", HabitCategory.PHYSICAL,
                90, 2, 2, 60);
        createQuickTask(raid, members.get(2 % size), "Posture check", "Make sure you sit and stand straight",
                HabitCategory.PHYSICAL, 90, 2, 2, 15);
        createQuickTask(raid, members.get(3 % size), "Positivity Check", "Write down 3 things positive about yourself",
                HabitCategory.EMOTIONAL, 90, 2, 2, 60);
        createQuickTask(raid, members.get(0 % size), "Gratefulness Check", "Write down 3 things you're grateful for",
                HabitCategory.COGNITIVE, 100, 4, 3, 45);
        createQuickTask(raid, members.get(1 % size), "Answer unread messages", "Reply to 3 unread messages",
                HabitCategory.COGNITIVE, 100, 4, 3, 60);
        createQuickTask(raid, members.get(2 % size), "Refill water bottle", "Refill and place it on your desk",
                HabitCategory.PHYSICAL, 100, 4, 3, 30);
        createQuickTask(raid, members.get(3 % size), "One positive message", "Send an encouraging message to someone",
                HabitCategory.EMOTIONAL, 100, 4, 3, 60);
        createQuickTask(raid, members.get(0 % size), "Plan top 3 tasks", "List your top 3 priorities for today",
                HabitCategory.COGNITIVE, 110, 5, 4, 60);
        createQuickTask(raid, members.get(1 % size), "Eye break", "Look away from the screen for 60 seconds",
                HabitCategory.EMOTIONAL, 110, 5, 4, 60);
        createQuickTask(raid, members.get(2 % size), "10 squats", "Do 10 bodyweight squats", HabitCategory.PHYSICAL,
                110, 5, 4, 60);
        createQuickTask(raid, members.get(3 % size), "Clear one small task", "Complete one pending micro-task",
                HabitCategory.COGNITIVE, 110, 5, 4, 60);
    }

    private void inviteAllGroupMembers(BossRaid raid, Group group) {
        for (User member : group.getUsers()) {
            if (raidParticipationRepository.findByBossRaidAndUser(raid, member).isPresent())
                continue;
            RaidParticipation p = new RaidParticipation();
            p.setUser(member);
            p.setBossRaid(raid);
            p.setAccepted(null);
            raidParticipationRepository.save(p);
        }
    }

    private boolean allTasksCompleted(BossRaid raid) {
        List<RaidTask> tasks = raidTaskRepository.findByRaid(raid);
        if (tasks.isEmpty())
            return false;
        for (RaidTask task : tasks) {
            if (raidTaskCompletionRepository.findByRaidTask(task).isEmpty())
                return false;
        }
        return true;
    }

    private boolean tryAutoSchedule(BossRaid raid, Group group, int windowDays) {
        List<Long> memberIds = group.getUsers().stream()
                .map(u -> u.getId())
                .collect(Collectors.toList());

        if (memberIds.isEmpty())
            return false;

        Instant from = Instant.now();
        Instant to = from.plus(windowDays, ChronoUnit.DAYS);

        List<FreeSlotGetDTO> freeSlots = calendarService.findGroupFreeSlots(memberIds, from, to);

        long requiredSeconds = raid.getDurationSeconds();
        for (FreeSlotGetDTO slot : freeSlots) {
            Instant slotStart = Instant.parse(slot.getStart());
            Instant slotEnd = Instant.parse(slot.getEnd());
            long slotSeconds = slotEnd.getEpochSecond() - slotStart.getEpochSecond();

            if (slotSeconds >= requiredSeconds) {
                raid.setScheduledTime(slotStart);
                bossRaidRepository.save(raid);
                return true;
            }
        }
        return false;
    }

    private void createCalendarEventsForRaid(BossRaid raid) {
        if (raid.getScheduledTime() == null)
            return;
        Instant start = raid.getScheduledTime();
        Instant end = start.plusSeconds(raid.getDurationSeconds());
        for (User member : raid.getGroup().getUsers()) {
            calendarService.addRaidEventToCalendar(member.getId(), raid.getName(), start, end);
        }
    }

    @Transactional
    public void autoScheduleRaidsForAllGroups() {
        Instant now = Instant.now();
        Instant threeDaysAgo = now.minus(3, ChronoUnit.DAYS);

        List<Group> allGroups = groupRepository.findAll();

        for (Group group : allGroups) {
            if (!bossRaidRepository.findByGroupIdAndStatusAndScheduledTimeAfter(
                    group.getId(), RaidStatus.SCHEDULED, now).isEmpty())
                continue;

            java.util.Optional<BossRaid> last = bossRaidRepository
                    .findTopByGroupIdAndStatusInOrderByEndedAtDesc(
                            group.getId(), List.of(RaidStatus.DEFEATED, RaidStatus.FAILED));
            if (last.isPresent() && last.get().getEndedAt() != null
                    && last.get().getEndedAt().isAfter(threeDaysAgo))
                continue;

            BossRaid newRaid = new BossRaid();
            newRaid.setGroup(group);
            newRaid.setName("Innere Schweinehund");
            newRaid.setDurationSeconds(1800);
            newRaid.setStatus(RaidStatus.SCHEDULED);
            newRaid.setHealth(1);
            newRaid.setMaxHealth(1);
            newRaid = bossRaidRepository.save(newRaid);

            List<User> members = getAliveGroupUsers(group);
            createQuickTasksForRaid(newRaid, members);
            int bossHealth = calculateBossHealth(newRaid, members.size());
            newRaid.setHealth(bossHealth);
            newRaid.setMaxHealth(bossHealth);
            newRaid = bossRaidRepository.save(newRaid);

            inviteAllGroupMembers(newRaid, group);

            if (tryAutoSchedule(newRaid, group, 7)) {
                createCalendarEventsForRaid(newRaid);
            }
        }
    }

    // ── Admin / testing helpers ──────────────────────────────────────────────

    @Transactional
    public void adminAutoScheduleForGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        BossRaid newRaid = new BossRaid();
        newRaid.setGroup(group);
        newRaid.setName("Innere Schweinehund");
        newRaid.setDurationSeconds(1800);
        newRaid.setStatus(RaidStatus.SCHEDULED);
        newRaid.setHealth(1);
        newRaid.setMaxHealth(1);
        newRaid = bossRaidRepository.save(newRaid);

        List<User> members = getAliveGroupUsers(group);
        createQuickTasksForRaid(newRaid, members);
        int bossHealth = calculateBossHealth(newRaid, members.size());
        newRaid.setHealth(bossHealth);
        newRaid.setMaxHealth(bossHealth);
        newRaid = bossRaidRepository.save(newRaid);

        inviteAllGroupMembers(newRaid, group);

        if (tryAutoSchedule(newRaid, group, 7)) {
            createCalendarEventsForRaid(newRaid);
        }
    }

    @Transactional
    public void adminScheduleForGroupWithEarliest(Long groupId, Instant earliest) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        BossRaid newRaid = new BossRaid();
        newRaid.setGroup(group);
        newRaid.setName("Innere Schweinehund");
        newRaid.setDurationSeconds(1800);
        newRaid.setStatus(RaidStatus.SCHEDULED);
        newRaid.setHealth(1);
        newRaid.setMaxHealth(1);
        newRaid = bossRaidRepository.save(newRaid);

        List<User> members = getAliveGroupUsers(group);
        createQuickTasksForRaid(newRaid, members);
        int bossHealth = calculateBossHealth(newRaid, members.size());
        newRaid.setHealth(bossHealth);
        newRaid.setMaxHealth(bossHealth);
        newRaid = bossRaidRepository.save(newRaid);

        inviteAllGroupMembers(newRaid, group);

        // Schedule to the earliest time (5 minutes from now)
        newRaid.setScheduledTime(earliest);
        bossRaidRepository.save(newRaid);
        createCalendarEventsForRaid(newRaid);
    }

    @Transactional
    public BossRaid adminStartRaidImmediately(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        BossRaid newRaid = new BossRaid();
        newRaid.setGroup(group);
        newRaid.setName("Innere Schweinehund");
        newRaid.setDurationSeconds(1800);
        newRaid.setStatus(RaidStatus.ACTIVE);
        newRaid.setStartedAt(Instant.now());
        newRaid.setHealth(1);
        newRaid.setMaxHealth(1);
        newRaid = bossRaidRepository.save(newRaid);

        List<User> members = getAliveGroupUsers(group);
        createQuickTasksForRaid(newRaid, members);
        int bossHealth = calculateBossHealth(newRaid, members.size());
        newRaid.setHealth(bossHealth);
        newRaid.setMaxHealth(bossHealth);
        newRaid = bossRaidRepository.save(newRaid);

        // Pre-accept all alive members
        for (User member : members) {
            RaidParticipation participation = new RaidParticipation();
            participation.setUser(member);
            participation.setBossRaid(newRaid);
            participation.setAccepted(true);
            raidParticipationRepository.save(participation);
        }
        // Also invite the rest of the group as pending
        inviteAllGroupMembers(newRaid, group);

        bossRaidRepository.save(newRaid);
        broadcastRaidUpdate(newRaid);

        return newRaid;
    }

    @Transactional
    public BossRaid adminFastForwardRaid(Long raidId, int seconds) {
        BossRaid raid = bossRaidRepository.findById(raidId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Raid not found"));
        if (raid.getStatus() != RaidStatus.SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only SCHEDULED raids can be fast-forwarded");
        }
        raid.setScheduledTime(Instant.now().plusSeconds(seconds));
        return bossRaidRepository.save(raid);
    }

    @Transactional
    public BossRaid adminForceCompleteRaid(Long raidId, String outcome) {
        BossRaid raid = bossRaidRepository.findById(raidId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Raid not found"));
        RaidStatus target = "DEFEATED".equalsIgnoreCase(outcome) ? RaidStatus.DEFEATED : RaidStatus.FAILED;
        endRaidWithRewards(raid, target);
        raid = bossRaidRepository.save(raid);
        broadcastRaidUpdate(raid);
        return raid;
    }

    @Transactional
    public void adminClearGroupRaids(Long groupId) {
        List<BossRaid> raids = bossRaidRepository.findByGroupId(groupId);
        for (BossRaid raid : raids) {
            deleteRaid(raid);
        }
    }

    private void deleteRaid(BossRaid raid) {
        List<RaidTask> tasks = raidTaskRepository.findByRaid(raid);
        for (RaidTask task : tasks) {
            raidTaskCompletionRepository.deleteAll(raidTaskCompletionRepository.findByRaidTask(task));
        }
        raidTaskRepository.deleteAll(tasks);
        raidParticipationRepository.deleteAll(raidParticipationRepository.findByBossRaidId(raid.getId()));
        bossRaidRepository.delete(raid);
    }

    private void broadcastRaidDeletion(BossRaid raid) {
        raidLiveService.broadcastRaidUpdate(
                raid.getId(),
                raid.getGroup().getId(),
                0,
                0,
                "DELETED",
                List.of());
    }
}

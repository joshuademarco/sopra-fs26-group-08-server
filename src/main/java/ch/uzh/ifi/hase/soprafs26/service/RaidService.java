package ch.uzh.ifi.hase.soprafs26.service;

import java.time.Instant;
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

import ch.uzh.ifi.hase.soprafs26.constant.RaidStatus;
import ch.uzh.ifi.hase.soprafs26.entity.BossRaid;
import ch.uzh.ifi.hase.soprafs26.entity.Group;
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
    private final LiveService liveService;

    @Autowired
    public RaidService(BossRaidRepository bossRaidRepository,
            RaidParticipationRepository raidParticipationRepository,
            UserRepository userRepository, RaidTaskRepository raidTaskRepository,
            RaidTaskCompletionRepository raidTaskCompletionRepository,
            CalendarService calendarService,
            GroupRepository groupRepository,
            LiveService liveService) {
        this.bossRaidRepository = bossRaidRepository;
        this.raidParticipationRepository = raidParticipationRepository;
        this.userRepository = userRepository;
        this.raidTaskRepository = raidTaskRepository;
        this.raidTaskCompletionRepository = raidTaskCompletionRepository;
        this.calendarService = calendarService;
        this.groupRepository = groupRepository;
        this.liveService = liveService;
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
        tryAutoSchedule(raid, group, windowDays);

        return raid;
    }

    public BossRaid rescheduleRaid(Long raidId, int windowDays) {
        BossRaid raid = bossRaidRepository.findById(raidId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Raid not found"));

        if (raid.getStatus() != RaidStatus.SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only SCHEDULED raids can be rescheduled");
        }

        tryAutoSchedule(raid, raid.getGroup(), windowDays);
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

            List<RaidMemberDTO> members = new ArrayList<>();
            for (User user : group.getUsers()) {
                RaidMemberDTO member = new RaidMemberDTO();
                member.setUserId(user.getId());
                member.setUsername(user.getUsername());
                member.setOnline(user.isOnline());
                RaidParticipation p = participationByUserId.get(user.getId());
                member.setJoined(p != null);
                member.setTasksCompleted(p != null ? p.getTasksCompleted() : 0);
                member.setTasksFailed(p != null ? p.getTasksFailed() : 0);
                member.setDamageDealt(p != null ? p.getDamageDealt() : 0);
                member.setHealth(user.getHealth());
                member.setMaxHealth(user.getMaxHealth());
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
            raid.startRaid();
            bossRaidRepository.save(raid);
            broadcastRaidUpdate(raid);
        }
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
                raid.endRaid(RaidStatus.FAILED);
                bossRaidRepository.save(raid);
                broadcastRaidUpdate(raid);
            }
        }
    }

    public RaidParticipation joinRaid(Long raidId, String token) {
        BossRaid raid = bossRaidRepository.findById(raidId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Raid not found"));

        User user = resolveUser(token);

        raidParticipationRepository.findByBossRaidAndUser(raid, user).ifPresent(p -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already joined this raid");
        });

        RaidParticipation participation = new RaidParticipation();
        participation.setUser(user);
        participation.setBossRaid(raid);
        return raidParticipationRepository.save(participation);
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

        raidTaskCompletionRepository.findByRaidTaskAndParticipation(task, participation).ifPresent(c -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Task already completed");
        });

        RaidTaskCompletion completion = new RaidTaskCompletion();
        completion.setParticipation(participation);
        completion.setRaidTask(task);
        completion.setSuccess(success);
        raidTaskCompletionRepository.save(completion);

        if (Boolean.TRUE.equals(success)) {
            int damage = task.getSuccessfulDamage() != null ? task.getSuccessfulDamage() : 0;
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
        bossRaidRepository.save(raid);

        broadcastRaidUpdate(raid);

        return completion;
    }

    // Keep session open due to LAZY relations; causes LazyInitializationException otherwise
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

                int groupDmg = task.getGroupDamage() != null ? task.getGroupDamage() : 0;
                if (groupDmg > 0) {
                    applyGroupDamageToMembers(raid.getId(), groupDmg);
                }

                broadcastRaidUpdate(raid);
            }
        }
    }

    private void applyGroupDamageToMembers(Long raidId, int groupDamage) {
        List<RaidParticipation> participations = raidParticipationRepository.findByBossRaidId(raidId);
        for (RaidParticipation participation : participations) {
            User member = participation.getUser();
            if (member.getHealth() == null) {
                continue;
            }

            member.setHealth(Math.max(0, member.getHealth() - groupDamage));
            userRepository.save(member);
        }
    }

    private void broadcastRaidUpdate(BossRaid raid) {
        liveService.broadcastRaidUpdate(
                raid.getId(),
                raid.getGroup().getId(),
                raid.getHealth(),
                raid.getMaxHealth(),
                raid.getStatus().name(),
                memberHealthSnapshot(raid));
    }

    private List<RaidMemberDTO> memberHealthSnapshot(BossRaid raid) {
        List<RaidMemberDTO> snapshot = new ArrayList<>();
        for (User user : raid.getGroup().getUsers()) {
            RaidMemberDTO m = new RaidMemberDTO();
            m.setUserId(user.getId());
            m.setHealth(user.getHealth());
            m.setMaxHealth(user.getMaxHealth());
            snapshot.add(m);
        }
        return snapshot;
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

    private void tryAutoSchedule(BossRaid raid, Group group, int windowDays) {
        List<Long> memberIds = group.getUsers().stream()
                .map(u -> u.getId())
                .collect(Collectors.toList());

        if (memberIds.isEmpty())
            return;

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
                return;
            }
        }
    }
}

package ch.uzh.ifi.hase.soprafs26.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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
import ch.uzh.ifi.hase.soprafs26.rest.dto.RaidPostDTO;

@Service
public class RaidService {

    private final BossRaidRepository bossRaidRepository;
    private final RaidParticipationRepository raidParticipationRepository;
    private final UserRepository userRepository;
    private final RaidTaskRepository raidTaskRepository;
    private final RaidTaskCompletionRepository raidTaskCompletionRepository;
    private final CalendarService calendarService;
    private final GroupRepository groupRepository;

    @Autowired
    public RaidService(BossRaidRepository bossRaidRepository,
            RaidParticipationRepository raidParticipationRepository,
            UserRepository userRepository, RaidTaskRepository raidTaskRepository,
            RaidTaskCompletionRepository raidTaskCompletionRepository,
            CalendarService calendarService,
            GroupRepository groupRepository) {
        this.bossRaidRepository = bossRaidRepository;
        this.raidParticipationRepository = raidParticipationRepository;
        this.userRepository = userRepository;
        this.raidTaskRepository = raidTaskRepository;
        this.raidTaskCompletionRepository = raidTaskCompletionRepository;
        this.calendarService = calendarService;
        this.groupRepository = groupRepository;
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

    public void activateDueRaids() {
        List<BossRaid> due = bossRaidRepository.findByStatusAndScheduledTimeBefore(RaidStatus.SCHEDULED, Instant.now());
        for (BossRaid raid : due) {
            raid.startRaid();
            bossRaidRepository.save(raid);
        }
    }

    public RaidParticipation joinRaid(Long raidId, Long userId) {
        BossRaid raid = bossRaidRepository.findById(raidId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Raid not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        raidParticipationRepository.findByBossRaidAndUser(raid, user).ifPresent(p -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already joined this raid");
        });

        RaidParticipation participation = new RaidParticipation();
        participation.setUser(user);
        participation.setBossRaid(raid);
        return raidParticipationRepository.save(participation);
    }

    public RaidTaskCompletion completeTask(Long raidId, Long userId, Long taskId, Boolean success) {
        BossRaid raid = bossRaidRepository.findById(raidId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Raid not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        RaidTask task = raidTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        RaidParticipation participation = raidParticipationRepository.findByBossRaidAndUser(raid, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not participating in this raid"));

        raidTaskCompletionRepository.findByRaidTaskAndParticipation(task, participation).ifPresent(c -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Task already completed");
        });

        RaidTaskCompletion completion = new RaidTaskCompletion();
        completion.setParticipation(participation);
        completion.setRaidTask(task);
        completion.setSuccess(success);
        raidTaskCompletionRepository.save(completion);

        if (success == true) {
            raid.applyDamage(task.getSuccessfulDamage());
        } else {
            // waiting for newest group entity for correct groupdamage handling
        }
        bossRaidRepository.save(raid);
        return completion;
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

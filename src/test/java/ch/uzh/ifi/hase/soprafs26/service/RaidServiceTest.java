package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.RaidStatus;
import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.rest.dto.FreeSlotGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RaidPostDTO;

public class RaidServiceTest {

    @Mock
    private BossRaidRepository bossRaidRepository;
    @Mock
    private RaidParticipationRepository raidParticipationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RaidTaskRepository raidTaskRepository;
    @Mock
    private RaidTaskCompletionRepository raidTaskCompletionRepository;
    @Mock
    private CalendarService calendarService;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private LiveService liveService;

    @InjectMocks
    private RaidService raidService;

    private User user;
    private Group group;
    private BossRaid raid;
    private RaidTask task;
    private RaidParticipation participation;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setToken("token");
        user.setHealth(100);
        user.setMaxHealth(100);

        group = new Group();
        group.setId(10L);
        group.setName("Raiders");
        group.setUsers(new HashSet<>(Set.of(user)));

        raid = new BossRaid();
        raid.setId(1L);
        raid.setGroup(group);
        raid.setName("Boss");
        raid.setHealth(500);
        raid.setMaxHealth(500);
        raid.setDurationSeconds(3600);
        raid.setStatus(RaidStatus.ACTIVE);
        raid.setStartedAt(Instant.now().minusSeconds(60));

        task = new RaidTask();
        task.setId(1L);
        task.setTitle("Task");
        task.setSuccessfulDamage(100);
        task.setGroupDamage(20);
        task.setTimeLimitSeconds(300);
        task.setTaskOrder(0);
        task.setRaid(raid);
        task.setAssignedUser(user);

        participation = new RaidParticipation();
        participation.setUser(user);
        participation.setBossRaid(raid);

        when(userRepository.findByToken("token")).thenReturn(user);
        when(bossRaidRepository.findById(1L)).thenReturn(Optional.of(raid));
        when(raidTaskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(raidParticipationRepository.findByBossRaidAndUser(raid, user)).thenReturn(Optional.of(participation));
        when(raidTaskCompletionRepository.findByRaidTaskAndParticipation(task, participation))
                .thenReturn(Optional.empty());
    }

    @Test
    public void completeTask_success_reducesBossHPAndTracksStats() {
        raidService.completeTask(1L, 1L, true, "token");

        assertEquals(400, raid.getHealth());
        assertEquals(1, participation.getTasksCompleted());
        assertEquals(100, participation.getDamageDealt());
    }

    @Test
    public void completeTask_success_killsBoss_statusBecomesDefeated() {
        raid.setHealth(100);
        task.setSuccessfulDamage(100);

        raidService.completeTask(1L, 1L, true, "token");

        assertEquals(RaidStatus.DEFEATED, raid.getStatus());
    }

    @Test
    public void completeTask_failure_incrementsTasksFailed_appliesGroupDamage() {
        when(raidParticipationRepository.findByBossRaidId(1L)).thenReturn(List.of(participation));

        raidService.completeTask(1L, 1L, false, "token");

        assertEquals(1, participation.getTasksFailed());
        assertEquals(80, user.getHealth());
    }

    @Test
    public void completeTask_raidNotActive_throwsConflict() {
        raid.setStatus(RaidStatus.SCHEDULED);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> raidService.completeTask(1L, 1L, true, "token"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    public void completeTask_notParticipating_throwsForbidden() {
        when(raidParticipationRepository.findByBossRaidAndUser(raid, user)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> raidService.completeTask(1L, 1L, true, "token"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    public void completeTask_alreadyCompleted_throwsConflict() {
        when(raidTaskCompletionRepository.findByRaidTaskAndParticipation(task, participation))
                .thenReturn(Optional.of(new RaidTaskCompletion()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> raidService.completeTask(1L, 1L, true, "token"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    public void joinRaid_success_savesParticipation() {
        when(raidParticipationRepository.findByBossRaidAndUser(raid, user)).thenReturn(Optional.empty());
        when(raidParticipationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        raidService.joinRaid(1L, "token");

        verify(raidParticipationRepository).save(any(RaidParticipation.class));
    }

    @Test
    public void joinRaid_alreadyJoined_throwsConflict() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> raidService.joinRaid(1L, "token"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    public void joinRaid_invalidToken_throwsUnauthorized() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> raidService.joinRaid(1L, null));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    public void createRaid_validInputs_setsScheduledStatusAndMatchingHealth() {
        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(calendarService.findGroupFreeSlots(any(), any(), any())).thenReturn(List.of());
        when(bossRaidRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RaidPostDTO dto = new RaidPostDTO();
        dto.setName("Boss");
        dto.setDurationSeconds(1800);
        dto.setHealth(1000);

        BossRaid result = raidService.createRaid(10L, dto);

        assertEquals(RaidStatus.SCHEDULED, result.getStatus());
        assertEquals(1000, result.getHealth());
        assertEquals(1000, result.getMaxHealth());
    }

    @Test
    public void createRaid_groupNotFound_throwsNotFound() {
        when(groupRepository.findById(99L)).thenReturn(Optional.empty());

        RaidPostDTO dto = new RaidPostDTO();
        dto.setName("Boss");
        dto.setDurationSeconds(1800);
        dto.setHealth(1000);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> raidService.createRaid(99L, dto));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void rescheduleRaid_raidNotScheduled_throwsConflict() {

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> raidService.rescheduleRaid(1L, 7));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    public void expireActiveRaids_expiredRaid_marksAsFailed() {
        raid.setDurationSeconds(10);
        raid.setStartedAt(Instant.now().minusSeconds(60));
        when(bossRaidRepository.findByStatus(RaidStatus.ACTIVE)).thenReturn(List.of(raid));

        raidService.expireActiveRaids();

        assertEquals(RaidStatus.FAILED, raid.getStatus());
        verify(bossRaidRepository).save(raid);
    }

    @Test
    public void expireActiveRaids_notExpired_raidRemainsActive() {
        when(bossRaidRepository.findByStatus(RaidStatus.ACTIVE)).thenReturn(List.of(raid));

        raidService.expireActiveRaids();

        assertEquals(RaidStatus.ACTIVE, raid.getStatus());
        verify(bossRaidRepository, never()).save(raid);
    }

    @Test
    public void activateDueRaids_scheduledRaidIsDue_startsRaid() {
        raid.setStatus(RaidStatus.SCHEDULED);
        when(bossRaidRepository.findByStatusAndScheduledTimeBefore(eq(RaidStatus.SCHEDULED), any()))
                .thenReturn(List.of(raid));

        raidService.activateDueRaids();

        assertEquals(RaidStatus.ACTIVE, raid.getStatus());
        assertNotNull(raid.getStartedAt());
    }

    @Test
    public void createRaid_freeSlotAvailable_setsScheduledTime() {
        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(bossRaidRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Instant slotStart = Instant.now().plusSeconds(3600);
        FreeSlotGetDTO slot = new FreeSlotGetDTO();
        slot.setStart(slotStart.toString());
        slot.setEnd(slotStart.plusSeconds(7200).toString());
        when(calendarService.findGroupFreeSlots(any(), any(), any())).thenReturn(List.of(slot));

        RaidPostDTO dto = new RaidPostDTO();
        dto.setName("Boss");
        dto.setDurationSeconds(1800);
        dto.setHealth(500);

        BossRaid result = raidService.createRaid(10L, dto);

        assertEquals(slotStart, result.getScheduledTime());
    }

    @Test
    public void rescheduleRaid_whenScheduled_doesNotThrow() {
        raid.setStatus(RaidStatus.SCHEDULED);
        when(bossRaidRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(calendarService.findGroupFreeSlots(any(), any(), any())).thenReturn(List.of());

        BossRaid result = raidService.rescheduleRaid(1L, 7);

        assertEquals(RaidStatus.SCHEDULED, result.getStatus());
    }

    @Test
    public void completeTask_failure_memberHealthNeverGoesBelowZero() {
        user.setHealth(5);
        task.setGroupDamage(50);
        when(raidParticipationRepository.findByBossRaidId(1L)).thenReturn(List.of(participation));

        raidService.completeTask(1L, 1L, false, "token");

        assertEquals(0, user.getHealth());
    }

    @Test
    public void completeTask_failure_zeroGroupDamage_doesNotTouchMemberHealth() {
        task.setGroupDamage(0);

        raidService.completeTask(1L, 1L, false, "token");

        assertEquals(100, user.getHealth());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void expireOverdueTasks_overdueTask_autoFails() {
        raid.setStartedAt(Instant.now().minusSeconds(700));
        
        when(bossRaidRepository.findByStatus(RaidStatus.ACTIVE)).thenReturn(List.of(raid));
        when(raidTaskRepository.findByRaid(raid)).thenReturn(new ArrayList<>(List.of(task)));
        when(raidTaskCompletionRepository.findByRaidTask(task)).thenReturn(List.of());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(raidParticipationRepository.findByBossRaidId(1L)).thenReturn(List.of(participation));

        raidService.expireOverdueTasks();

        assertEquals(1, participation.getTasksFailed());
    }
}

package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.BossRaid;
import ch.uzh.ifi.hase.soprafs26.entity.RaidParticipation;
import ch.uzh.ifi.hase.soprafs26.entity.RaidTask;
import ch.uzh.ifi.hase.soprafs26.entity.RaidTaskCompletion;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.BossRaidRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RaidParticipationRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RaidTaskCompletionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RaidTaskRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

@Service
public class RaidService {

    private final BossRaidRepository bossRaidRepository;
    private final RaidParticipationRepository raidParticipationRepository;
    private final UserRepository userRepository;
    private final RaidTaskRepository raidTaskRepository;
    private final RaidTaskCompletionRepository raidTaskCompletionRepository;

    @Autowired
    public RaidService(BossRaidRepository bossRaidRepository,
            RaidParticipationRepository raidParticipationRepository,
            UserRepository userRepository, RaidTaskRepository raidTaskRepository,
            RaidTaskCompletionRepository raidTaskCompletionRepository) {
        this.bossRaidRepository = bossRaidRepository;
        this.raidParticipationRepository = raidParticipationRepository;
        this.userRepository = userRepository;
        this.raidTaskRepository = raidTaskRepository;
        this.raidTaskCompletionRepository = raidTaskCompletionRepository;
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

    public BossRaid getRaid(Long raidId) {
        BossRaid raid = bossRaidRepository.findById(raidId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Raid not found"));
        return raid;
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
}

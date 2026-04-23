package ch.uzh.ifi.hase.soprafs26.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.RaidParticipation;
import ch.uzh.ifi.hase.soprafs26.entity.RaidTask;
import ch.uzh.ifi.hase.soprafs26.entity.RaidTaskCompletion;

@Repository
public interface RaidTaskCompletionRepository extends JpaRepository<RaidTaskCompletion, Long> {
    Optional<RaidTaskCompletion> findByRaidTaskAndParticipation(RaidTask raidTask, RaidParticipation participation);
}

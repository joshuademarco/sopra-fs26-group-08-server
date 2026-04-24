package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ch.uzh.ifi.hase.soprafs26.entity.BossRaid;
import ch.uzh.ifi.hase.soprafs26.entity.RaidParticipation;
import ch.uzh.ifi.hase.soprafs26.entity.User;


@Repository
public interface RaidParticipationRepository extends JpaRepository<RaidParticipation, Long> {
    List<RaidParticipation> findByUserId(Long id);

    List<RaidParticipation> findByBossRaidId(Long id);

    Optional<RaidParticipation> findByBossRaidAndUser(BossRaid bossRaid, User user);
}
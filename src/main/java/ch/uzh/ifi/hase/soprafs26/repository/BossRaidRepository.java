package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ch.uzh.ifi.hase.soprafs26.constant.RaidStatus;
import ch.uzh.ifi.hase.soprafs26.entity.BossRaid;

@Repository("bossRaidRepository")
public interface BossRaidRepository extends JpaRepository<BossRaid, Long> {
    List<BossRaid> findByGroupId(Long groupId);
    List<BossRaid> findByStatusAndScheduledTimeBefore(RaidStatus status, Instant time);
}
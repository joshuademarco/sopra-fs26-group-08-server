package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import ch.uzh.ifi.hase.soprafs26.entity.BossRaid;

@Repository("bossRaidRepository")
public interface BossRaidRepository extends JpaRepository<BossRaid, Long> {
    BossRaid findByGroupId(Long groupId);
}
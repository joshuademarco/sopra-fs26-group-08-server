package ch.uzh.ifi.hase.soprafs26.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.BossRaid;
import ch.uzh.ifi.hase.soprafs26.entity.RaidTask;

@Repository
public interface RaidTaskRepository extends JpaRepository<RaidTask, Long> {
    List<RaidTask> findByRaid(BossRaid raid);
}

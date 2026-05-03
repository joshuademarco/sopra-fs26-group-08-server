package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository("habitRepository")
public interface HabitRepository extends JpaRepository<Habit, Long> {

    List<Habit> findByUserId(Long userId);
    List<Habit> findByCompletedFalseAndDueAtBefore(Instant now);
    List<Habit> findByCompletedTrueAndDueAtBefore(Instant now);
}

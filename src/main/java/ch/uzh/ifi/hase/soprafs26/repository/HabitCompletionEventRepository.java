package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.HabitCompletionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("habitCompletionEventRepository")
public interface HabitCompletionEventRepository
        extends JpaRepository<HabitCompletionEvent, Long> {

    //all completion events for a specific habit (history)
    List<HabitCompletionEvent> findByHabitId(Long habitId);

    //all completion events by a specific user (for progress tracking)
    List<HabitCompletionEvent> findByUserId(Long userId);
}

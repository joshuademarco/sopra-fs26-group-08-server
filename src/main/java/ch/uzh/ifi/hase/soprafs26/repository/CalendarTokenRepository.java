package ch.uzh.ifi.hase.soprafs26.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.CalendarToken;

@Repository("calendarTokenRepository")
public interface CalendarTokenRepository extends JpaRepository<CalendarToken, Long> {
    Optional<CalendarToken> findByUserId(Long userId);
}

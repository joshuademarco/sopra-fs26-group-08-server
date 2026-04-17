package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Character;

@Repository("characterRepository")
public interface CharacterRepository extends JpaRepository<Character, Long> {
  Character findByUserId(Long userId);
}

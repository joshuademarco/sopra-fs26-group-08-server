// CharacterAchievementRepository.java
package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.Achievement;
import ch.uzh.ifi.hase.soprafs26.entity.CharacterAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CharacterAchievementRepository extends JpaRepository<CharacterAchievement, Long> {
    List<CharacterAchievement> findByCharacter(Character character);
    boolean existsByCharacterAndAchievement(Character character, Achievement achievement);
}

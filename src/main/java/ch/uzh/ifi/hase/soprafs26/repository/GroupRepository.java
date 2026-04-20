package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Group;

@Repository("groupRepository")
public interface GroupRepository extends JpaRepository<Group, Long> {
  Group findByName(String name);
}
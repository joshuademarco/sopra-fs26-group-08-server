package ch.uzh.ifi.hase.soprafs26.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Group;
import ch.uzh.ifi.hase.soprafs26.repository.GroupRepository;

@Service
@Transactional
public class GroupService {

  private final GroupRepository groupRepository;

  @Autowired
  public GroupService(@Qualifier("groupRepository") GroupRepository groupRepository) {
    this.groupRepository = groupRepository;
  }

  public Group createGroup(Group newGroup) {
    if (groupRepository.findByName(newGroup.getName()) != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Group name already exists");
    }
    return groupRepository.save(newGroup);
  }

  public List<Group> getAllGroups() {
    return this.groupRepository.findAll();
  }
}
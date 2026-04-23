package ch.uzh.ifi.hase.soprafs26.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Group;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

@Service
@Transactional
public class GroupService {

  private final GroupRepository groupRepository;
  private final UserRepository userRepository; 

  public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
    this.groupRepository = groupRepository;
    this.userRepository = userRepository; 
  }

  public Group createGroup(Group newGroup, String token) {
    User creator = userRepository.findByToken(token);
    if (creator == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired or invalid token.");
    }

    newGroup.setCreatedBy(creator.getUsername());
    newGroup.setCreatedAt(LocalDateTime.now());
        
    Group savedGroup = groupRepository.save(newGroup); 
        
    creator.addGroup(savedGroup);
    userRepository.save(creator);
        
    return savedGroup;
  }

  public Group joinGroup(String groupName, String password, String token) {
    User user = userRepository.findByToken(token);
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired.");
    }

    Group group = groupRepository.findByName(groupName);
    if (group == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found.");
    }

    if (!group.getPassword().equals(password)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid password.");
    }

    if (user.getGroups().contains(group)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are already a member of this group.");
    } 

    user.addGroup(group);
    userRepository.save(user);

    return group;
  }

  public List<Group> getMyGroups(String token) {
    User user = userRepository.findByToken(token);
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired.");
    }
        
    return new ArrayList<>(user.getGroups());
  }
}
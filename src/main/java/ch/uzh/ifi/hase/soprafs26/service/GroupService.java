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
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupPutDTO;

@Service
@Transactional
public class GroupService {

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  public GroupService(GroupRepository groupRepository, UserRepository userRepository,
      NotificationService notificationService) {
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
    this.notificationService = notificationService;
  }

  public Group createGroup(Group newGroup, String token) {
    User creator = userRepository.findByToken(token);
    if (creator == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired or invalid token.");
    }

    if (groupRepository.findByName(newGroup.getName()) != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name already exists.");
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
    for (User member : group.getUsers()) {
      if (!member.getId().equals(user.getId())) {
        notificationService.sendUserJoinedGroupEmail(user, group);
      }
    }

    return group;
  }

  public void leaveGroup(Long groupId, String token) {
    User user = userRepository.findByToken(token);
    if (user == null)
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired.");

    Group group = groupRepository.findById(groupId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found."));

    if (group.getCreatedBy().equals(user.getUsername()))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Owner cannot leave. Delete the group instead.");

    if (!user.getGroups().contains(group))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not a member of this group.");

    user.removeGroup(group);
    userRepository.save(user);
  }

  public Group updateGroup(Long groupId, GroupPutDTO dto, String token) {
    User owner = userRepository.findByToken(token);
    if (owner == null)
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired.");

    Group group = groupRepository.findById(groupId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found."));

    if (!group.getCreatedBy().equals(owner.getUsername()))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can edit this group.");

    if (dto.getName() != null && !dto.getName().isBlank())
      group.setName(dto.getName());
    if (dto.getPassword() != null && !dto.getPassword().isBlank())
      group.setPassword(dto.getPassword());

    return groupRepository.save(group);
  }

  public void deleteGroup(Long groupId, String token) {
    User owner = userRepository.findByToken(token);
    if (owner == null)
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired.");

    Group group = groupRepository.findById(groupId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found."));

    if (!group.getCreatedBy().equals(owner.getUsername()))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can delete this group.");

    for (User u : new java.util.HashSet<>(group.getUsers())) {
      u.getGroups().remove(group);
      userRepository.save(u);
    }
    groupRepository.delete(group);
  }

  public List<Group> getMyGroups(String token) {
    User user = userRepository.findByToken(token);
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired.");
    }

    return new ArrayList<>(user.getGroups());
  }
}
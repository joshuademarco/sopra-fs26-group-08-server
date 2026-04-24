package ch.uzh.ifi.hase.soprafs26.service;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Group;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

public class GroupServiceTest {

  @Mock
  private GroupRepository groupRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private GroupService groupService;

  private User groupOwner;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    groupOwner = new User();
    groupOwner.setId(1L);
    groupOwner.setUsername("creator");
    groupOwner.setToken("token");
    groupOwner.setGroups(new HashSet<>());

    when(userRepository.findByToken("token")).thenReturn(groupOwner);
    when(groupRepository.save(any())).thenAnswer(invocation -> {
      Group groupArg = invocation.getArgument(0);
      groupArg.setId(1L);
      return groupArg;
    });
  }

  @Test
  public void createGroup_validInputs_success() {
    Group newGroup = new Group();
    newGroup.setName("Test Group");
    newGroup.setPassword("secret");

    Group createdGroup = groupService.createGroup(newGroup, "token");

    verify(groupRepository).save(any());
    verify(userRepository).save(groupOwner);

    assertEquals(1L, createdGroup.getId());
    assertEquals("Test Group", createdGroup.getName());
    assertEquals("creator", createdGroup.getCreatedBy());
    assertNotNull(createdGroup.getCreatedAt());
    assertEquals(1, groupOwner.getGroups().size());
  }

  @Test
  public void createGroup_invalidToken_throwsUnauthorized() {
    when(userRepository.findByToken("bad-token")).thenReturn(null);

    Group sampleGroup = new Group();
    sampleGroup.setName("Test Group");
    sampleGroup.setPassword("secret");

    assertThrows(ResponseStatusException.class, () -> groupService.createGroup(sampleGroup, "bad-token"));
  }

  @Test
  public void joinGroup_validInputs_success() {
    Group existingGroup = new Group();
    existingGroup.setId(2L);
    existingGroup.setName("Test Group");
    existingGroup.setPassword("secret");

    when(groupRepository.findByName("Test Group")).thenReturn(existingGroup);

    Group result = groupService.joinGroup("Test Group", "secret", "token");

    verify(userRepository).save(groupOwner);
    assertEquals(existingGroup, result);
    assertEquals(1, groupOwner.getGroups().size());
  }

  @Test
  public void joinGroup_groupNotFound_throwsNotFound() {
    when(groupRepository.findByName("Unknown")).thenReturn(null);

    assertThrows(ResponseStatusException.class, () -> groupService.joinGroup("Unknown", "secret", "token"));
  }

  @Test
  public void joinGroup_invalidPassword_throwsForbidden() {
    Group existingGroup = new Group();
    existingGroup.setName("Test Group");
    existingGroup.setPassword("other");

    when(groupRepository.findByName("Test Group")).thenReturn(existingGroup);

    assertThrows(ResponseStatusException.class, () -> groupService.joinGroup("Test Group", "secret", "token"));
  }

  @Test
  public void joinGroup_alreadyMember_throwsBadRequest() {
    Group existingGroup = new Group();
    existingGroup.setId(2L);
    existingGroup.setName("Test Group");
    existingGroup.setPassword("secret");

    groupOwner.getGroups().add(existingGroup);
    when(groupRepository.findByName("Test Group")).thenReturn(existingGroup);

    assertThrows(ResponseStatusException.class, () -> groupService.joinGroup("Test Group", "secret", "token"));
  }

  @Test
  public void getMyGroups_validToken_success() {
    Group existingGroup = new Group();
    existingGroup.setId(2L);
    existingGroup.setName("Test Group");
    groupOwner.getGroups().add(existingGroup);

    List<Group> myGroups = groupService.getMyGroups("token");

    assertEquals(1, myGroups.size());
    assertEquals("Test Group", myGroups.get(0).getName());
  }

  @Test
  public void getMyGroups_invalidToken_throwsUnauthorized() {
    when(userRepository.findByToken("somethingInvalid")).thenReturn(null);

    assertThrows(ResponseStatusException.class, () -> groupService.getMyGroups("somethingInvalid"));
  }
}
